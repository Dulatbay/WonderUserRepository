package kz.wonder.wonderuserrepository.mappers;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import kz.wonder.wonderuserrepository.dto.xml.KaspiCatalog;
import kz.wonder.wonderuserrepository.entities.AbstractEntity;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.Product;
import kz.wonder.wonderuserrepository.entities.ProductPrice;
import kz.wonder.wonderuserrepository.repositories.ProductPriceRepository;
import kz.wonder.wonderuserrepository.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.JAXB_SCHEMA_LOCATION;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.XML_SCHEMA_INSTANCE;

@Component
@RequiredArgsConstructor
public class ProductXmlMapper {
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;

    public KaspiCatalog buildKaspiCatalog(List<Product> listOfProducts, KaspiToken kaspiToken) {
        KaspiCatalog kaspiCatalog = new KaspiCatalog();
        kaspiCatalog.setCompany(kaspiToken.getSellerName());
        kaspiCatalog.setMerchantid(kaspiToken.getSellerId());
        kaspiCatalog.setOffers(getOffers(listOfProducts));
        return kaspiCatalog;
    }

    public KaspiCatalog buildKaspiCatalogInChunks(String keycloakUserId, KaspiToken kaspiToken) {
        KaspiCatalog kaspiCatalog = new KaspiCatalog();
        kaspiCatalog.setCompany(kaspiToken.getSellerName());
        kaspiCatalog.setMerchantid(kaspiToken.getSellerId());
        kaspiCatalog.setOffers(new ArrayList<>());


        int pageSize = 100;
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Product> productsChunk;

        do {
            productsChunk = productRepository.findAllSellerProductsWithPrices(keycloakUserId, pageable);
            if (!productsChunk.isEmpty()) {
                kaspiCatalog.getOffers().addAll(getOffers(productsChunk.toList()));
            }
            pageable = pageable.next();
        } while (!productsChunk.isEmpty());

        return kaspiCatalog;
    }

    public Marshaller initJAXBContextAndProperties() throws JAXBException {
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
        final var listOfProductPrices = new HashSet<>(productPriceRepository.findPricesByProductIds(listOfProducts.stream().map(AbstractEntity::getId).toList()));

        Map<Long, Set<ProductPrice>> pricesMap = listOfProductPrices.stream()
                .collect(Collectors.groupingBy(price -> price.getProduct().getId(), Collectors.toSet()));

        for (var product : listOfProducts) {
            product.setPrices(pricesMap.getOrDefault(product.getId(), new HashSet<>()));
        }


        return listOfProducts.stream().map(this::mapToOffer).collect(Collectors.toList());
    }

    public String marshalObjectToXML(KaspiCatalog kaspiCatalog, Marshaller marshaller) throws JAXBException {
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
                        price.getKaspiCity()
                                .getKaspiStores()
                                .forEach(kaspiStore -> {
                                    KaspiCatalog.Offer.Availability availability = new KaspiCatalog.Offer.Availability();
                                    availability.setAvailable((price.getPrice() != null && price.getPrice() != 0) ? "yes" : "no");
                                    availability.setStoreId(kaspiStore.getKaspiId());
                                    availabilities.add(availability);
                                });


                        var kaspiCity = price.getKaspiCity();

                        KaspiCatalog.Offer.CityPrice cityPrice = new KaspiCatalog.Offer.CityPrice();
                        cityPrice.setCityId(kaspiCity.getId().toString());
                        cityPrice.setPrice(kaspiCity.isEnabled() ? price.getPrice().toString() : "0");
                        cityPrices.add(cityPrice);
                    });
        } else {
            var price = optionalMainPrice.get();
            product.getPrices()
                    .forEach(p -> {
                        price.getKaspiCity()
                                .getKaspiStores()
                                .forEach(kaspiStore -> {
                                    KaspiCatalog.Offer.Availability availability = new KaspiCatalog.Offer.Availability();
                                    availability.setAvailable((price.getPrice() != null && price.getPrice() != 0) ? "yes" : "no");
                                    availability.setStoreId(kaspiStore.getKaspiId());
                                    availabilities.add(availability);
                                });

                        var kaspiCity = price.getKaspiCity();
                        KaspiCatalog.Offer.CityPrice cityPrice = new KaspiCatalog.Offer.CityPrice();
                        cityPrice.setCityId(kaspiCity.getId().toString());
                        cityPrice.setPrice(kaspiCity.isEnabled() ? price.getPrice().toString() : "0");
                        cityPrices.add(cityPrice);
                    });
        }


        offer.setAvailabilities(availabilities);
        offer.setCityprices(cityPrices);
        return offer;
    }
}
