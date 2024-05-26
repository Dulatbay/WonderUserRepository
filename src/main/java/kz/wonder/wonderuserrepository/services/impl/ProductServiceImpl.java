package kz.wonder.wonderuserrepository.services.impl;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.ProductPriceChangeRequest;
import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.dto.response.ProductPriceResponse;
import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import kz.wonder.wonderuserrepository.dto.xml.KaspiCatalog;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.ProductPriceRepository;
import kz.wonder.wonderuserrepository.repositories.ProductRepository;
import kz.wonder.wonderuserrepository.services.FileService;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private static final String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String JAXB_SCHEMA_LOCATION = "kaspiShopping http://kaspi.kz/kaspishopping.xsd";
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final FileService fileService;

    @Transactional
    @Override
    public void processExcelFile(MultipartFile excelFile, String keycloakUserId) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
             ForkJoinPool customThreadPool = new ForkJoinPool(300)) {
            final Map<String, KaspiCity> cityCache = Collections.synchronizedMap(new HashMap<>());

            log.info("{} sheets loaded", workbook.getNumberOfSheets());

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("File must have at least one page!");
            }

            // get products page
            Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header rows
            for (int i = 0; i < 3 && rowIterator.hasNext(); i++) {
                rowIterator.next();
            }

            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("Send file by requirements!!");
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
            throw new IllegalArgumentException("File process failed");
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new IllegalArgumentException("File process failed");
        }
    }

    private Product processRow(Row row, String keycloakUserId, Map<String, KaspiCity> cityCache) {
        String vendorCode = getStringFromExcelCell(row.getCell(0));
        if (vendorCode.isEmpty()) {
            return null;
        }

        log.info("#{}, Processing product code: {}, user's keycloak id: {}", row.getRowNum() - 2, vendorCode, keycloakUserId);

        String name = getStringFromExcelCell(row.getCell(1));
        String link = row.getCell(2) == null ? null : getStringFromExcelCell(row.getCell(2));
        String isPublic = getStringFromExcelCell(row.getCell(3));
        double tradePrice = row.getCell(4).getNumericCellValue();
        double priceAlmaty = row.getCell(5).getNumericCellValue();
        double priceAstana = row.getCell(6).getNumericCellValue();
        double priceShymkent = row.getCell(7).getNumericCellValue();

        Product product = processProduct(vendorCode, name, link, isPublic, tradePrice, keycloakUserId);
        processProductPrices(product, priceAlmaty, priceAstana, priceShymkent, cityCache);

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
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));
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
    public String generateOfProductsXmlByKeycloakId(String keycloakId) throws IOException, JAXBException {
        final var listOfProducts = productRepository.findAllByKeycloakId(keycloakId);
        final var kaspiToken = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                        "Kaspi token doesn't exists",
                        "Create your kaspi token before request"));
        KaspiCatalog kaspiCatalog = buildKaspiCatalog(listOfProducts, kaspiToken);

        log.info("keycloakId: {}, kaspiCatalog: {}", keycloakId, kaspiCatalog);
        Marshaller marshaller = initJAXBContextAndProperties();
        String xmlContent = marshalObjectToXML(kaspiCatalog, marshaller);
        return fileService.save(xmlContent.getBytes(), "xml");
    }

    @Override
    public void deleteProductById(String keycloakId, Long productId) {

        final var product = productRepository.findByIdAndKeycloakId(productId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Product doesn't exist"));
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
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));

        if (product.isEnabled() == isPublished) {
            throw new IllegalArgumentException("The product is already has same publish state");
        }

        product.setEnabled(isPublished);

        productRepository.save(product);
    }

    @Override
    @Transactional
    public void changePrice(String keycloakId, ProductPriceChangeRequest productPriceChangeRequest) {
        if (productPriceChangeRequest.getPriceList() != null && !productPriceChangeRequest.getPriceList().isEmpty())
            updatePrice(keycloakId, productPriceChangeRequest.getPriceList());
        if (productPriceChangeRequest.getMainPriceList() != null && !productPriceChangeRequest.getMainPriceList().isEmpty())
            updateMainCities(keycloakId, productPriceChangeRequest.getMainPriceList());
    }

    private void updateMainCities(String keycloakId, List<ProductPriceChangeRequest.MainPrice> mainPrices) {
        mainPrices
                .forEach(price -> {

                    var product = productRepository.findByIdAndKeycloakId(price.getProductId(), keycloakId)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));


                    if (price.getMainCityId() == -1) {
                        var priceToDelete = product.getMainCityPrice();
                        if (priceToDelete == null)
                            throw new IllegalArgumentException("Price already unselected");

                        product.setMainCityPrice(null);
                        productRepository.save(product);
                        productPriceRepository.delete(priceToDelete);
                        return;
                    }

                    var city = kaspiCityRepository.findById(price.getMainCityId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));

                    var productPrice = productPriceRepository.findByProductIdAndKaspiCityName(product.getId(), city.getName())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));

                    product.setMainCityPrice(productPrice);
                    productRepository.save(product);
                });
    }

    private void updatePrice(String keycloakId, List<ProductPriceChangeRequest.Price> prices) {
        prices
                .forEach(price -> {
                    var product = productRepository.findByIdAndKeycloakId(price.getProductId(), keycloakId)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));

                    var city = kaspiCityRepository.findById(price.getCityId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));

                    var productPrice = productPriceRepository.findByProductIdAndKaspiCityName(product.getId(), city.getName())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));

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
