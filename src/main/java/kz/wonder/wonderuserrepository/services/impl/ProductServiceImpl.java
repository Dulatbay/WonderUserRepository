package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.filemanager.client.api.FileManagerApi;
import kz.wonder.wonderuserrepository.dto.params.ProductSearchParams;
import kz.wonder.wonderuserrepository.dto.request.ProductPriceChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.ProductSizeChangeRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.dto.xml.KaspiCatalog;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.ProductMapper;
import kz.wonder.wonderuserrepository.mappers.ProductXmlMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.store.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.ForbiddenException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.FILE_MANAGER_XML_DIR;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final UserRepository userRepository;
    private final ProductSizeRepository productSizeRepository;
    private final FileManagerApi fileManagerApi;
    private final ProductMapper productMapper;
    private final ProductXmlMapper productXmlMapper;
    private final MessageSource messageSource;

    @Transactional
    @Override
    public void processExcelFile(MultipartFile excelFile, String keycloakUserId) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
             ForkJoinPool customThreadPool = new ForkJoinPool(300)) {
            final Map<String, KaspiCity> cityCache = Collections.synchronizedMap(new HashMap<>());


            var token = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakUserId)
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.access-denied", null, LocaleContextHolder.getLocale())));


            log.info("{} sheets loaded", workbook.getNumberOfSheets());

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(messageSource.getMessage("services-impl.product-service-impl.file-must-have-at-least-one-page", null, LocaleContextHolder.getLocale()));
            }



            // get products page
            Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header rows
            for (int i = 0; i < 3 && rowIterator.hasNext(); i++) {
                rowIterator.next();
            }

            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException(messageSource.getMessage("services-impl.product-service-impl.send-file-according-to-requirements", null, LocaleContextHolder.getLocale()));
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

            token.setXmlUpdated(false);
            kaspiTokenRepository.save(token);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Parallel processing error: ", e);
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.product-service-impl.file-processing-failed", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.product-service-impl.file-processing-failed", null, LocaleContextHolder.getLocale()));
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
                .findByOriginalVendorCodeAndKeycloakIdAndDeletedIsFalse(originVendorCode, keycloakUserId)
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
        if (isPublic.equalsIgnoreCase(messageSource.getMessage("services-impl.product-service-impl.yes", null, LocaleContextHolder.getLocale())))
            return Boolean.TRUE;
        if (isPublic.equalsIgnoreCase(messageSource.getMessage("services-impl.product-service-impl.no", null, LocaleContextHolder.getLocale())))
            return Boolean.FALSE;
        return Boolean.parseBoolean(isPublic);
    }

    private void processProductPrices(Product product, double priceAlmaty, double priceAstana, double priceShymkent, Map<String, KaspiCity> cityCache) {

        var cityAlmaty = getCachedCity("Алматы", cityCache);
        var cityAstana = getCachedCity("Астана", cityCache);
        var cityShymkent = getCachedCity("Шымкент", cityCache);

        var priceAtAlmaty = processProductPrice(product, cityAlmaty, priceAlmaty);
        var priceAtAstana = processProductPrice(product, cityAstana, priceAstana);
        var priceAtShymkent = processProductPrice(product, cityShymkent, priceShymkent);


        product.setPrices(new HashSet<>(Arrays.asList(priceAtAlmaty, priceAtAstana, priceAtShymkent)));
    }

    private KaspiCity getCachedCity(String cityName, Map<String, KaspiCity> cityCache) {
        return cityCache.computeIfAbsent(cityName, this::getCityByName);
    }

    private KaspiCity getCityByName(String cityName) {
        return kaspiCityRepository.findByName(cityName)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.city-not-found", null, LocaleContextHolder.getLocale())));
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
        var products = productRepository.findAllByKeycloakId(keycloakUserId, searchValue, searchValue, isPublished, pageable);

        return products.map(productMapper::mapToResponse);
    }


    @Override
    public String generateOfProductsXmlByKeycloakId(String keycloakId) {
        final var kaspiToken = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                        messageSource.getMessage("services-impl.product-service-impl.kaspi-token-not-found", null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage("services-impl.product-service-impl.create-your-kaspi-token-before-requesting", null, LocaleContextHolder.getLocale())));


        if (kaspiToken.isXmlUpdated())
            return kaspiToken.getPathToXml();


        return generateAndUploadXML(kaspiToken);
    }

    @Deprecated
    private String generateAndUpload(KaspiToken kaspiToken) {
        try {
            final var wonderUser = kaspiToken.getWonderUser();
            KaspiCatalog kaspiCatalog = productXmlMapper.buildKaspiCatalogInChunks(wonderUser.getKeycloakId(), kaspiToken);

            Marshaller marshaller = productXmlMapper.initJAXBContextAndProperties();
            String xmlContent = productXmlMapper.marshalObjectToXML(kaspiCatalog, marshaller);

            var fileName = wonderUser.getKeycloakId() + ".xml";
            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, "text/xml", xmlContent.getBytes());
            var resultOfUploading = fileManagerApi.uploadFiles(FILE_MANAGER_XML_DIR, List.of(multipartFile), false).getBody();

            kaspiToken.setPathToXml(fileName);
            kaspiToken.setXmlUpdated(true);
            kaspiToken.setXmlUpdatedAt(LocalDateTime.now(ZONE_ID));
            kaspiTokenRepository.save(kaspiToken);

            return resultOfUploading.get(0);
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new IllegalStateException(messageSource.getMessage("services-impl.product-service-impl.error-generating-xml", null, LocaleContextHolder.getLocale()));
        }
    }

    private String generateAndUploadXML(KaspiToken kaspiToken) {
        try {
            final var wonderUser = kaspiToken.getWonderUser();
            KaspiCatalog kaspiCatalog = productXmlMapper.buildKaspiCatalogInChunks(wonderUser.getKeycloakId(), kaspiToken);

            String xmlContent = productXmlMapper.convertKaspiCatalogToXML(kaspiCatalog);

            String fileName = wonderUser.getKeycloakId() + ".xml";
            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, "text/xml", xmlContent.getBytes());

            List<String> resultOfUploading = fileManagerApi.uploadFiles(FILE_MANAGER_XML_DIR, List.of(multipartFile), false).getBody();

            kaspiToken.setPathToXml(fileName);
            kaspiToken.setXmlUpdated(true);
            kaspiToken.setXmlUpdatedAt(LocalDateTime.now(ZONE_ID));
            kaspiTokenRepository.save(kaspiToken);

            return resultOfUploading.getFirst();

        } catch (XMLStreamException e) {
            log.error("Exception: ", e);
            throw new IllegalStateException(messageSource.getMessage("services-impl.product-service-impl.error-generating-xml", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void deleteProductById(String keycloakId, Long productId) {
        final var product = productRepository.findByIdAndKeycloakIdAndDeletedIsFalse(productId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));
        log.info("Product with id {} was deleted", productId);

        product.setDeleted(true);
        product.setEnabled(false);
        productRepository.save(product);


        var token = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), "Ваш аккаунт не имеет доступа к ресурсу"));

        token.setXmlUpdated(false);
        kaspiTokenRepository.save(token);
    }


    // todo: refactoring
    @Override
    public ProductPriceResponse getProductsPrices(String keycloakId, boolean isSuperAdmin, Pageable pageable, Boolean isPublished, String searchValue) {
        Page<Product> products;
        Map<Long, CityResponse> cityResponseMap = new HashMap<>();

        if (isSuperAdmin) {
            products = productRepository.findAllBy(searchValue, searchValue, isPublished, pageable);
        } else {
            log.info("FETCH PRODUCTS STARTED");
            products = productRepository.findAllByKeycloakId(keycloakId, searchValue, searchValue, isPublished, pageable);
            log.info("FETCH PRODUCTS ENDED");
        }

        List<Long> productIds = products.getContent().stream().map(Product::getId).collect(Collectors.toList());

        if (!productIds.isEmpty()) {
            log.info("FETCH ProductPrice STARTED");
            List<ProductPrice> prices = productPriceRepository.findPricesByProductIds(productIds);
            log.info("FETCH ProductPrice ENDED");
            log.info("FETCH SupplyBoxProduct STARTED");
            List<SupplyBoxProduct> supplyBoxes = supplyBoxProductsRepository.findSupplyBoxesByProductIds(productIds);
            log.info("FETCH SupplyBoxProduct ENDED");

            Map<Long, Set<ProductPrice>> pricesMap = prices.stream()
                    .collect(Collectors.groupingBy(pp -> pp.getProduct().getId(), Collectors.toSet()));

            Map<Long, Set<SupplyBoxProduct>> supplyBoxesMap = supplyBoxes.stream()
                    .collect(Collectors.groupingBy(sbp -> sbp.getProduct().getId(), Collectors.toSet()));

            products.forEach(product -> {
                product.setPrices(pricesMap.computeIfAbsent(product.getId(), k -> new HashSet<>()));
                product.setSupplyBoxProducts(supplyBoxesMap.computeIfAbsent(product.getId(), k -> new HashSet<>()));
            });
        }

        ProductPriceResponse.Content response = new ProductPriceResponse.Content();


        products
                .forEach(product -> {
                    var mainCityPrice = Optional.ofNullable(product.getMainCityPrice()).orElse(new ProductPrice());

                    if (mainCityPrice.getId() != null) {
                        cityResponseMap.computeIfAbsent(mainCityPrice.getId(), k -> {
                            CityResponse cityResponse = new CityResponse();
                            cityResponse.setId(mainCityPrice.getId());
                            cityResponse.setName(mainCityPrice.getKaspiCity().getName());
                            cityResponse.setCode(mainCityPrice.getKaspiCity().getCode());
                            cityResponse.setEnabled(mainCityPrice.getKaspiCity().isEnabled());
                            return cityResponse;
                        });
                    }


                    var count = product.getSupplyBoxProducts().stream().filter(p -> p.getState() == ProductStateInStore.ACCEPTED).count();

                    var productInfo = ProductPriceResponse.Content.ProductInfo.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .vendorCode(product.getVendorCode())
                            .count(count)
                            .isPublished(product.isEnabled())
                            .prices(product.getPrices().stream().map(price -> {
                                var city = price.getKaspiCity();

                                cityResponseMap.computeIfAbsent(city.getId(), k -> {
                                    CityResponse cityResponse = new CityResponse();
                                    cityResponse.setId(city.getId());
                                    cityResponse.setName(city.getName());
                                    cityResponse.setCode(city.getCode());
                                    cityResponse.setEnabled(city.isEnabled());
                                    return cityResponse;
                                });

                                return ProductMapper.mapProductPrice(product, price, city);
                            }).toList())
                            .mainPriceCityId(mainCityPrice.getId())
                            .build();

                    response.getProducts().add(productInfo);
                });

        response.setCities(cityResponseMap.values().stream().toList());

        ProductPriceResponse productPriceResponse = new ProductPriceResponse();
        productPriceResponse.setContent(response);
        productPriceResponse.setPage(pageable.getPageNumber());
        productPriceResponse.setSize(pageable.getPageSize());
        productPriceResponse.setLast(products.isLast());
        productPriceResponse.setTotalPages(products.getTotalPages());
        productPriceResponse.setTotalElements(products.getTotalElements());

        return productPriceResponse;
    }

    @Override
    public void changePublish(String keycloakId, Long productId, Boolean isPublished) {
        var product = productRepository.findByIdAndKeycloakIdAndDeletedIsFalse(productId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));

        if (product.isEnabled() == isPublished) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.product-service-impl.product-already-has-same-publication-status", null, LocaleContextHolder.getLocale()));
        }


        product.setEnabled(isPublished);

        productRepository.save(product);

        var token = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.your-account-does-not-have-access-to-the-resource", null, LocaleContextHolder.getLocale())));

        token.setXmlUpdated(false);
        kaspiTokenRepository.save(token);
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

        var token = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.access-denied", null, LocaleContextHolder.getLocale())));

        token.setXmlUpdated(false);
        kaspiTokenRepository.save(token);
    }

    @Override
    public Page<ProductSearchResponse> searchByParams(ProductSearchParams productSearchParams, PageRequest
            pageRequest, String employeeKeycloakId) {
        final var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(employeeKeycloakId)
                .orElseThrow(() -> new ForbiddenException(HttpStatus.FORBIDDEN.getReasonPhrase()));

        final var store = storeEmployee.getKaspiStore();

        var supplyBoxProducts = supplyBoxProductsRepository.findByParams(
                store.getId(),
                productSearchParams.getSearchValue() != null ? productSearchParams.getSearchValue().toLowerCase().trim() : "",
                productSearchParams.isByProductName(),
                productSearchParams.isByVendorCode(),
                productSearchParams.isByArticle(),
                pageRequest
        );

        return supplyBoxProducts.map(productMapper::mapProductSearchResponse);
    }

    @Override
    public void changeSize(String originVendorCode, ProductSizeChangeRequest productSizeChangeRequest, String
            keycloakId) {
        final var wonderUser = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ForbiddenException(HttpStatus.FORBIDDEN.getReasonPhrase()));

        boolean productExists = productRepository.existsByOriginalVendorCodeAndDeletedIsFalse(originVendorCode);


        if (!productExists) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.product-service-impl.product-not-found", null, LocaleContextHolder.getLocale()));
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

        var token = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), "Ваш аккаунт не имеет доступа к ресурсу"));
        token.setXmlUpdated(false);
        kaspiTokenRepository.save(token);
    }

    @Override
    public Page<ProductWithSize> getProductsSizes(ProductSearchParams productSearchParams, Boolean
            isSizeScanned, String keycloakId, PageRequest pageRequest) {
        final var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new ForbiddenException(HttpStatus.FORBIDDEN.getReasonPhrase()));

        final var store = storeEmployee.getKaspiStore();

        var supplyBoxProducts = supplyBoxProductsRepository.findByParamsUniqueByProduct(
                store.getId(),
                productSearchParams.getSearchValue() != null ? productSearchParams.getSearchValue().toLowerCase().trim() : "",
                productSearchParams.isByProductName(),
                productSearchParams.isByVendorCode(),
                isSizeScanned,
                pageRequest
        );

        return supplyBoxProducts.map(productMapper::mapProductsSizesResponse);
    }

    @Override
    @Transactional
    public void generateXmls() {
        var tokens = kaspiTokenRepository.findAllXmlsToUpdate();

        log.info("Found tokens to generating: {}", tokens.size());

        tokens
                .parallelStream()
                .forEach(this::generateAndUploadXML);
        log.info("Generated xmls: {}", tokens.size());
    }

    private void updateMainCities(String keycloakId, List<ProductPriceChangeRequest.MainPrice> mainPrices) {
        mainPrices
                .forEach(price -> {

                    var product = productRepository.findByIdAndKeycloakIdAndDeletedIsFalse(price.getProductId(), keycloakId)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));


                    if (price.getMainCityId() == -1) {
                        var priceToDelete = product.getMainCityPrice();
                        if (priceToDelete == null)
                            throw new IllegalArgumentException(messageSource.getMessage("services-impl.product-service-impl.price-already-not-selected", null, LocaleContextHolder.getLocale()));

                        product.setMainCityPrice(null);
                        productRepository.save(product);
                        productPriceRepository.delete(priceToDelete);
                        return;
                    }

                    var city = kaspiCityRepository.findById(price.getMainCityId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.city-not-found", null, LocaleContextHolder.getLocale())));

                    var productPrice = productPriceRepository.findByProductIdAndKaspiCityName(product.getId(), city.getName())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));


                    product.setMainCityPrice(productPrice);
                    productRepository.save(product);
                });
    }

    private void updatePrice(String keycloakId, List<ProductPriceChangeRequest.Price> prices) {
        prices
                .forEach(price -> {
                    var product = productRepository.findByIdAndKeycloakIdAndDeletedIsFalse(price.getProductId(), keycloakId)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));

                    var city = kaspiCityRepository.findById(price.getCityId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.city-not-found", null, LocaleContextHolder.getLocale())));

                    var productPrice = productPriceRepository.findByProductIdAndKaspiCityName(product.getId(), city.getName())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.product-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));


                    productPrice.setPrice(price.getPrice());
                    productPriceRepository.save(productPrice);
                });
    }
}
