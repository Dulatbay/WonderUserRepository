package kz.wonder.wonderuserrepository.services.impl;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import kz.wonder.wonderuserrepository.dto.xml.KaspiCatalog;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.Product;
import kz.wonder.wonderuserrepository.entities.ProductPrice;
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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final FileService fileService;
    private static final String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String JAXB_SCHEMA_LOCATION = "kaspiShopping http://kaspi.kz/kaspishopping.xsd";


    @Override
    @Transactional
    public List<ProductResponse> processExcelFile(MultipartFile excelFile, String keycloakUserId) {
        try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
            List<ProductResponse> productResponses = new ArrayList<>();

            if (workbook.getNumberOfSheets() == 0)
                throw new IllegalArgumentException("File must have at least one page!");
            Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
                rowIterator.next();
                rowIterator.next();
            }

            // todo: improve TL
            if (!rowIterator.hasNext())
                throw new IllegalArgumentException("Send file by requirements!!");
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String vendorCode = getStringFromExcelCell(row.getCell(0));
                if (vendorCode.isEmpty())
                    continue;

                Product product = processProduct(row, keycloakUserId, vendorCode);

                processProductPrices(product, row);

                productResponses.add(mapToResponse(product));
            }
            return productResponses;
        } catch (IllegalStateException e) {
            log.error("IllegalStateException: ", e);
            throw new IllegalArgumentException("File process failed");
        } catch (Exception e) {
            log.info("Exception: ", e);
            throw new IllegalArgumentException("File process failed");
        }
    }

    private Product processProduct(Row row, String keycloakUserId, String vendorCode) {
        Product product = productRepository
                .findByVendorCodeAndKeycloakId(vendorCode, keycloakUserId)
                .orElse(new Product());
        product.setVendorCode(vendorCode);
        product.setName(row.getCell(1).getStringCellValue());
        product.setLink(row.getCell(2) == null ? null : row.getCell(2).getStringCellValue());

        var isPublic = row.getCell(3).getStringCellValue();
        product.setEnabled(getBooleanFromString(isPublic));
        product.setTradePrice(row.getCell(4).getNumericCellValue());
        product.setKeycloakId(keycloakUserId);
        product.setDeleted(false);
        return productRepository.save(product);
    }

    private Boolean getBooleanFromString(String isPublic) {
        if (isPublic.equalsIgnoreCase("да")) return Boolean.TRUE;
        if (isPublic.equalsIgnoreCase("нет")) return Boolean.FALSE;
        return Boolean.parseBoolean(isPublic);
    }

    private void processProductPrices(Product product, Row row) {
        final String CITY_ALMATY = "Алматы";
        final String CITY_ASTANA = "Астана";
        final String CITY_SHYMKENT = "Шымкент";

        var cityAlmaty = getCityByName(CITY_ALMATY);
        var cityAstana = getCityByName(CITY_ASTANA);
        var cityShymkent = getCityByName(CITY_SHYMKENT);

        var priceAtAlmaty = processProductPrice(product, cityAlmaty, row.getCell(5).getNumericCellValue());
        var priceAtAstana = processProductPrice(product, cityAstana, row.getCell(6).getNumericCellValue());
        var priceAtShymkent = processProductPrice(product, cityShymkent, row.getCell(7).getNumericCellValue());

        product.setPrices(new ArrayList<>());
        product.getPrices().add(priceAtAlmaty);
        product.getPrices().add(priceAtAstana);
        product.getPrices().add(priceAtShymkent);
    }

    private KaspiCity getCityByName(String cityName) {
        return kaspiCityRepository.findByName(cityName)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));
    }

    private ProductPrice processProductPrice(Product product, KaspiCity city, Double priceValue) {
        var productPrice = productPriceRepository.findByProductAndKaspiCityName(product, city.getName())
                .orElse(new ProductPrice(city, product, priceValue));
        productPrice.setKaspiCity(city);
        productPrice.setPrice(priceValue);
        productPrice.setUpdatedAt(LocalDateTime.now());
        return productPriceRepository.save(productPrice);
    }

    @Override
    public List<ProductResponse> getProductsByKeycloakId(String keycloakUserId) {
        return productRepository.findAllByKeycloakId(keycloakUserId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
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

        productRepository.delete(product);
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
        KaspiCatalog.Offer offer = new KaspiCatalog.Offer();
        offer.setSku(product.getVendorCode());
        offer.setModel(product.getName());

        List<KaspiCatalog.Offer.Availability> availabilities = product.getPrices().stream()
                .map(price -> {
                    KaspiCatalog.Offer.Availability availability = new KaspiCatalog.Offer.Availability();
                    availability.setAvailable(price.getPrice() != null ? "yes" : "no");
                    availability.setStoreId(price.getKaspiCity().getId().toString());
                    return availability;
                })
                .toList();

        List<KaspiCatalog.Offer.CityPrice> cityPrices = product.getPrices().stream()
                .map(price -> {
                    KaspiCatalog.Offer.CityPrice cityPrice = new KaspiCatalog.Offer.CityPrice();
                    cityPrice.setCityId(price.getKaspiCity().getId().toString());
                    cityPrice.setPrice(price.getPrice().toString());
                    return cityPrice;
                })
                .toList();

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
