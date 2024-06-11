package kz.wonder.wonderuserrepository.services.impl;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import jakarta.transaction.Transactional;
import kz.wonder.filemanager.client.api.FileManagerApi;
import kz.wonder.wonderuserrepository.dto.request.ProductPriceChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.ProductSearchRequest;
import kz.wonder.wonderuserrepository.dto.request.ProductSizeChangeRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.dto.xml.KaspiCatalog;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.FileService;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.ForbiddenException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.JAXB_SCHEMA_LOCATION;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.XML_SCHEMA_INSTANCE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final FileService fileService;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final UserRepository userRepository;
    private final ProductSizeRepository productSizeRepository;
    private final FileManagerApi fileManagerApi;
    private final KeycloakService keycloakService;

    @Transactional
    @Override
    public void processExcelFile(MultipartFile excelFile, String keycloakUserId) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
             ForkJoinPool customThreadPool = new ForkJoinPool(300)) {
            final Map<String, KaspiCity> cityCache = Collections.synchronizedMap(new HashMap<>());

            log.info("{} sheets loaded", workbook.getNumberOfSheets());

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("В файле должна быть хотя бы одна страница!");
            }

            // get products page
            Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header rows
            for (int i = 0; i < 3 && rowIterator.hasNext(); i++) {
                rowIterator.next();
            }

            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("Отправить файл по требованиям!!");
            }

            List<Row> rows = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(rowIterator, Spliterator.ORDERED), false
            ).toList();

            var productResponses = customThreadPool.submit(() ->
                    rows.parallelStream()
                            .map(row -> processRow(row, keycloakUserId, cityCache))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            ).get();

            int batchSize = 500;
            for (int i = 0; i < productResponses.size(); i += batchSize) {
                var batch = productResponses.subList(i, Math.min(productResponses.size(), i + batchSize));
                productRepository.saveAll(batch);
            }

            log.info("cities cache size: {}", cityCache.size());
            log.info("Product responses with size: {}", productResponses.size());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Parallel processing error: ", e);
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Обработка файла не удалась");
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new IllegalArgumentException("Обработка файла не удалась");
        }
    }

    private Product processRow(Row row, String keycloakUserId, Map<String, KaspiCity> cityCache) {
        String vendorCode = getStringFromExcelCell(row.getCell(0));
        if (vendorCode.isEmpty()) {
            return null;
        }

        String name = getStringFromExcelCell(row.getCell(1));
        String link = row.getCell(2) == null ? null : getStringFromExcelCell(row.getCell(2));
        String isPublic = getStringFromExcelCell(row.getCell(3));
        double tradePrice = row.getCell(4).getNumericCellValue();
        double priceAlmaty = row.getCell(5).getNumericCellValue();
        double priceAstana = row.getCell(6).getNumericCellValue();
        double priceShymkent = row.getCell(7).getNumericCellValue();

        Product product = processProduct(vendorCode, name, link, isPublic, tradePrice, keycloakUserId);
        processProductPrices(product, priceAlmaty, priceAstana, priceShymkent, cityCache);

        log.info("#{}, Processed product code: {}, user's keycloak id: {}, prices size: {}", row.getRowNum() - 2, vendorCode, keycloakUserId, product.getPrices().size());


        return product;
    }

    private Product processProduct(String vendorCode, String name, String link, String isPublic, double tradePrice, String keycloakUserId) {
        var originVendorCode = vendorCode.split("_")[0];
        Product product = productRepository
                .findByOriginalVendorCodeAndKeycloakId(originVendorCode, keycloakUserId)
                .orElse(new Product());
        product.setVendorCode(vendorCode);
        product.setOriginalVendorCode(originVendorCode);
        product.setName(name);
        product.setLink(link);

        product.setEnabled(getBooleanFromString(isPublic));
        product.setTradePrice(tradePrice);
        product.setKeycloakId(keycloakUserId);
        product.setDeleted(false);
        return product;
    }


    private Boolean getBooleanFromString(String isPublic) {
        if (isPublic.equalsIgnoreCase("да")) return Boolean.TRUE;
        if (isPublic.equalsIgnoreCase("нет")) return Boolean.FALSE;
        return Boolean.parseBoolean(isPublic);
    }

    private void processProductPrices(Product product, double priceAlmaty, double priceAstana, double priceShymkent, Map<String, KaspiCity> cityCache) {
        final String CITY_ALMATY = "Алматы";
        final String CITY_ASTANA = "Астана";
        final String CITY_SHYMKENT = "Шымкент";

        var cityAlmaty = getCachedCity(CITY_ALMATY, cityCache);
        var cityAstana = getCachedCity(CITY_ASTANA, cityCache);
        var cityShymkent = getCachedCity(CITY_SHYMKENT, cityCache);

        var priceAtAlmaty = processProductPrice(product, cityAlmaty, priceAlmaty);
        var priceAtAstana = processProductPrice(product, cityAstana, priceAstana);
        var priceAtShymkent = processProductPrice(product, cityShymkent, priceShymkent);


        product.setPrices(new ArrayList<>(Arrays.asList(priceAtAlmaty, priceAtAstana, priceAtShymkent)));
    }

    private KaspiCity getCachedCity(String cityName, Map<String, KaspiCity> cityCache) {
        return cityCache.computeIfAbsent(cityName, this::getCityByName);
    }

    private KaspiCity getCityByName(String cityName) {
        return kaspiCityRepository.findByName(cityName)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Город не существует"));
    }

    private ProductPrice processProductPrice(Product product, KaspiCity city, Double priceValue) {
        var productPrice = productPriceRepository.findByProductIdAndKaspiCityName(product.getId(), city.getName())
                .orElse(new ProductPrice(city, product, priceValue));
        productPrice.setKaspiCity(city);
        productPrice.setPrice(priceValue);
        return productPrice;
    }


    @Override
    public Page<ProductResponse> findAllByKeycloakId(String keycloakUserId, Pageable pageable, Boolean isPublished, String searchValue) {
        log.info("Retrieving products with keycloak id: {}", keycloakUserId);
        return productRepository.findByParams(keycloakUserId, searchValue, searchValue, isPublished, pageable)
                .map(this::mapToResponse);
    }


    @Override
    public String generateOfProductsXmlByKeycloakId(String keycloakId) throws JAXBException {
        final var listOfProducts = productRepository.findAllByKeycloakId(keycloakId)
                .subList(0, 10);
        final var kaspiToken = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                        "Kaspi токен не существует",
                        "Создай свой kaspi токен перед запросом"));
  
        final var wonderUser = kaspiToken.getWonderUser();
  
        KaspiCatalog kaspiCatalog = buildKaspiCatalog(listOfProducts, kaspiToken);

        Marshaller marshaller = initJAXBContextAndProperties();
        String xmlContent = marshalObjectToXML(kaspiCatalog, marshaller);

        var fileName = wonderUser.getKeycloakId() + ".xml";

        MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, "text/xml", xmlContent.getBytes());

        var resultOfUploading = fileManagerApi.uploadFiles("xml", List.of(multipartFile)).getBody();

        kaspiToken.setPathToXml(fileName);
        kaspiTokenRepository.save(kaspiToken);

        return resultOfUploading;
    }

    @Override
    public void deleteProductById(String keycloakId, Long productId) {

        final var product = productRepository.findByIdAndKeycloakId(productId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Товар не существует"));
        log.info("Product with id {} was deleted", productId);
        productRepository.delete(product);
    }

    // todo: refactoring
    @Override
    public Page<ProductPriceResponse> getProductsPrices(String keycloakId, boolean isSuperAdmin, Pageable pageable, Boolean isPublished, String searchValue) {
        Page<Product> products;
        Map<Long, CityResponse> cityResponseMap = new HashMap<>();


        if (isSuperAdmin) {
            products = productRepository.findAllBy(searchValue, searchValue, isPublished, pageable);
        } else {
            products = productRepository.findAllByKeycloakId(keycloakId, searchValue, searchValue, isPublished, pageable);
        }

        List<ProductPriceResponse.ProductInfo> response = new ArrayList<>();

        products
                .forEach(product -> {
                    var count = product.getSupplyBoxes().stream().filter(p -> p.getState() == ProductStateInStore.ACCEPTED).count();

                    var productInfo = ProductPriceResponse.ProductInfo.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .vendorCode(product.getVendorCode())
                            .count(count)
                            .isPublished(product.isEnabled())
                            // todo: improve tl
                            .prices(product.getPrices().stream().map(price -> {
                                var productPrice = new ProductPriceResponse.ProductPrice();
                                var city = price.getKaspiCity();

                                // todo: сделал поставку в город, где не указана цена

                                productPrice.setCityId(city.getId());
                                productPrice.setCityName(city.getName());
                                productPrice.setCount(product.getSupplyBoxes()
                                        .stream()
                                        .filter(p ->
                                                p.getState() == ProductStateInStore.ACCEPTED
                                                        && p.getSupplyBox().getSupply().getKaspiStore().getKaspiCity().getId().equals(city.getId())
                                        )
                                        .count());
                                productPrice.setPrice(price.getPrice());
                                return productPrice;
                            }).toList())
                            .build();

                    product.getPrices()
                            .forEach(price -> {
                                var city = price.getKaspiCity();
                                cityResponseMap.computeIfAbsent(city.getId(), k -> {
                                    CityResponse cityResponse = new CityResponse();
                                    cityResponse.setId(city.getId());
                                    cityResponse.setName(city.getName());
                                    cityResponse.setCode(city.getCode());
                                    cityResponse.setEnabled(city.isEnabled());
                                    return cityResponse;
                                });
                            });

                    response.add(productInfo);
                });

        ProductPriceResponse productPriceResponse = new ProductPriceResponse();
        productPriceResponse.setProducts(response);
        productPriceResponse.setCities(cityResponseMap.values().stream().toList());

        return new PageImpl<>(new ArrayList<>(Collections.singleton(productPriceResponse)), pageable, products.getTotalElements());
    }

    @Override
    public void changePublish(String keycloakId, Long productId, Boolean isPublished) {
        var product = productRepository.findByIdAndKeycloakId(productId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

        if (product.isEnabled() == isPublished) {
            throw new IllegalArgumentException("Товар уже имеет такое же состояние публикации");
        }

        product.setEnabled(isPublished);

        productRepository.save(product);
    }

    @Override
    @Transactional
    public void changePrice(String keycloakId, ProductPriceChangeRequest productPriceChangeRequest) {
        if (productPriceChangeRequest.getPriceList() != null) {
            updatePrice(keycloakId, productPriceChangeRequest.getPriceList());
        }
        if (productPriceChangeRequest.getMainPriceList() != null) {
            updateMainCities(keycloakId, productPriceChangeRequest.getMainPriceList());
        }
    }

    @Override
    public Page<ProductSearchResponse> searchByParams(ProductSearchRequest productSearchRequest, PageRequest pageRequest, String employeeKeycloakId) {
        final var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(employeeKeycloakId)
                .orElseThrow(() -> new ForbiddenException(HttpStatus.FORBIDDEN.getReasonPhrase()));

        final var store = storeEmployee.getKaspiStore();

        var supplyBoxProducts = supplyBoxProductsRepository.findByParams(
                Boolean.TRUE.equals(productSearchRequest.isByArticle()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByProductName()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByShopName()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByCellCode()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByVendorCode()) ? productSearchRequest.getSearchValue() : null,
                store.getId(),
                pageRequest
        );

        return supplyBoxProducts.map(this::toProductSearchResponse);
    }

    @Override
    public void changeSize(String originVendorCode, ProductSizeChangeRequest productSizeChangeRequest, String keycloakId) {
        final var wonderUser = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ForbiddenException(HttpStatus.FORBIDDEN.getReasonPhrase()));

        boolean productExists = productRepository.existsByOriginalVendorCode(originVendorCode);


        if (!productExists) {
            throw new IllegalArgumentException("Товар не существует");
        }

        var productSize = productSizeRepository.findByOriginVendorCode(originVendorCode)
                .orElse(new ProductSize());

        productSize.setOriginVendorCode(originVendorCode);
        productSize.setAuthor(wonderUser);
        productSize.setWidth(productSizeChangeRequest.getWidth());
        productSize.setHeight(productSizeChangeRequest.getHeight());
        productSize.setLength(productSizeChangeRequest.getLength());
        productSize.setWeight(productSizeChangeRequest.getWeight());
        productSize.setComment(productSizeChangeRequest.getComment());

        productSizeRepository.save(productSize);
    }

    @Override
    public Page<ProductWithSize> getProductsSizes(ProductSearchRequest productSearchRequest, String keycloakId, PageRequest pageRequest) {
        final var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new ForbiddenException(HttpStatus.FORBIDDEN.getReasonPhrase()));

        final var store = storeEmployee.getKaspiStore();

        var supplyBoxProducts = supplyBoxProductsRepository.findByParams(
                Boolean.TRUE.equals(productSearchRequest.isByArticle()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByProductName()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByShopName()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByCellCode()) ? productSearchRequest.getSearchValue() : null,
                Boolean.TRUE.equals(productSearchRequest.isByVendorCode()) ? productSearchRequest.getSearchValue() : null,
                store.getId(),
                pageRequest
        );

        return supplyBoxProducts.map(this::toProductsSizesResponse);
    }

    private ProductWithSize toProductsSizesResponse(SupplyBoxProduct supplyBoxProduct) {
        final var product = supplyBoxProduct.getProduct();
        final var size = productSizeRepository.findByOriginVendorCode(product.getOriginalVendorCode())
                .orElse(new ProductSize());

        ProductWithSize productWithSize = new ProductWithSize();
        productWithSize.setProductName(product.getName());
        productWithSize.setProductArticle(supplyBoxProduct.getArticle());
        productWithSize.setWeight(size.getWeight());
        productWithSize.setHeight(size.getHeight());
        productWithSize.setLength(size.getLength());
        productWithSize.setWidth(size.getWidth());
        productWithSize.setComment(size.getComment());
        productWithSize.setVendorCode(product.getVendorCode());
        productWithSize.setState(supplyBoxProduct.getState());


        return productWithSize;
    }

    private ProductSearchResponse toProductSearchResponse(SupplyBoxProduct supplyBoxProduct) {
        var product = supplyBoxProduct.getProduct();
        var token = kaspiTokenRepository.findByWonderUserKeycloakId(product.getKeycloakId())
                .orElseThrow(() -> new IllegalArgumentException("Возможно, пользователь был удален"));
        var storeCellProduct = supplyBoxProduct.getStoreCellProduct();

        ProductSearchResponse productSearchResponse = new ProductSearchResponse();
        productSearchResponse.setProductId(product.getId());
        productSearchResponse.setProductName(product.getName());
        productSearchResponse.setPrice(product.getTradePrice());
        productSearchResponse.setVendorCode(product.getVendorCode());
        productSearchResponse.setShopName(token.getSellerName());
        productSearchResponse.setCellCode(storeCellProduct.getStoreCell().getCode());
        productSearchResponse.setArticle(supplyBoxProduct.getArticle());

        return productSearchResponse;
    }

    private void updateMainCities(String keycloakId, List<ProductPriceChangeRequest.MainPrice> mainPrices) {
        mainPrices
                .forEach(price -> {

                    var product = productRepository.findByIdAndKeycloakId(price.getProductId(), keycloakId)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));


                    if (price.getMainCityId() == -1) {
                        var priceToDelete = product.getMainCityPrice();
                        if (priceToDelete == null)
                            throw new IllegalArgumentException("Цена уже не выбрана");

                        product.setMainCityPrice(null);
                        productRepository.save(product);
                        productPriceRepository.delete(priceToDelete);
                        return;
                    }

                    var city = kaspiCityRepository.findById(price.getMainCityId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Город не существует"));

                    var productPrice = productPriceRepository.findByProductIdAndKaspiCityName(product.getId(), city.getName())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

                    product.setMainCityPrice(productPrice);
                    productRepository.save(product);
                });
    }

    private void updatePrice(String keycloakId, List<ProductPriceChangeRequest.Price> prices) {
        prices
                .forEach(price -> {
                    var product = productRepository.findByIdAndKeycloakId(price.getProductId(), keycloakId)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

                    var city = kaspiCityRepository.findById(price.getCityId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Город не существует"));

                    var productPrice = productPriceRepository.findByProductIdAndKaspiCityName(product.getId(), city.getName())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

                    productPrice.setPrice(price.getPrice());
                    productPriceRepository.save(productPrice);
                });
    }

    private KaspiCatalog buildKaspiCatalog(List<Product> listOfProducts, KaspiToken kaspiToken) {
        KaspiCatalog kaspiCatalog = new KaspiCatalog();
        kaspiCatalog.setCompany(kaspiToken.getSellerName());
        kaspiCatalog.setMerchantid(kaspiToken.getSellerId());
        kaspiCatalog.setOffers(getOffers(listOfProducts));
        return kaspiCatalog;
    }

    private Marshaller initJAXBContextAndProperties() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(KaspiCatalog.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, JAXB_SCHEMA_LOCATION);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                if (XML_SCHEMA_INSTANCE.equals(namespaceUri)) {
                    return "xsi";
                }
                return suggestion;
            }

            @Override
            public String[] getPreDeclaredNamespaceUris() {
                return new String[]{XML_SCHEMA_INSTANCE};
            }
        });
        return marshaller;
    }

    private List<KaspiCatalog.Offer> getOffers(List<Product> listOfProducts) {
        return listOfProducts.stream().map(this::mapToOffer).collect(Collectors.toList());
    }

    private String marshalObjectToXML(KaspiCatalog kaspiCatalog, Marshaller marshaller) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(kaspiCatalog, writer);
        return writer.toString();
    }

    private KaspiCatalog.Offer mapToOffer(Product product) {
        List<KaspiCatalog.Offer.Availability> availabilities = new ArrayList<>();
        List<KaspiCatalog.Offer.CityPrice> cityPrices = new ArrayList<>();
        KaspiCatalog.Offer offer = new KaspiCatalog.Offer();
        offer.setSku(product.getVendorCode());
        offer.setModel(product.getName());

        var optionalMainPrice = Optional.ofNullable(product.getMainCityPrice());


        if (optionalMainPrice.isEmpty()) {
            product.getPrices()
                    .forEach(price -> {
                        KaspiCatalog.Offer.Availability availability = new KaspiCatalog.Offer.Availability();
                        availability.setAvailable((price.getPrice() != null && price.getPrice() != 0) ? "yes" : "no");
                        availability.setStoreId(price.getKaspiCity().getId().toString());
                        availabilities.add(availability);

                        KaspiCatalog.Offer.CityPrice cityPrice = new KaspiCatalog.Offer.CityPrice();
                        cityPrice.setCityId(price.getKaspiCity().getId().toString());
                        cityPrice.setPrice(price.getPrice().toString());
                        cityPrices.add(cityPrice);
                    });
        } else {
            var price = optionalMainPrice.get();
            product.getPrices()
                    .forEach(p -> {
                        KaspiCatalog.Offer.Availability availability = new KaspiCatalog.Offer.Availability();
                        availability.setAvailable((price.getPrice() != null && price.getPrice() != 0) ? "yes" : "no");
                        availability.setStoreId(price.getKaspiCity().getId().toString());
                        availabilities.add(availability);

                        KaspiCatalog.Offer.CityPrice cityPrice = new KaspiCatalog.Offer.CityPrice();
                        cityPrice.setCityId(price.getKaspiCity().getId().toString());
                        cityPrice.setPrice(price.getPrice().toString());
                        cityPrices.add(cityPrice);
                    });
        }


        offer.setAvailabilities(availabilities);
        offer.setCityprices(cityPrices);
        return offer;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .enabled(product.isEnabled())
                .name(product.getName())
                .vendorCode(product.getVendorCode())
                .keycloakUserId(product.getKeycloakId())
                .mainPriceCityId(product.getMainCityPrice() == null ? null : product.getMainCityPrice().getId())
                .prices(
                        product.getPrices().stream().map(
                                productPrice ->
                                        ProductResponse.ProductPriceResponse.builder()
                                                .price(productPrice.getPrice())
                                                .cityName(productPrice.getKaspiCity().getName())
                                                .build()
                        ).collect(Collectors.toList())
                )

                .build();
    }
}
