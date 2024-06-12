package kz.wonder.wonderuserrepository.services.impl;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.dto.response.SupplyAdminResponse;
import kz.wonder.wonderuserrepository.dto.xml.KaspiCatalog;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.Product;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.SellerMapper;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.SellerRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.services.FileService;
import kz.wonder.wonderuserrepository.services.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.JAXB_SCHEMA_LOCATION;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.XML_SCHEMA_INSTANCE;


@Slf4j
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final UserRepository userRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final KaspiApi kaspiApi;
    private final SellerMapper sellerMapper;
    private final SellerRepository sellerRepository;
    private final FileService fileService;


    @Override
    public void createSellerUser(SellerRegistrationRequest sellerRegistrationRequest) {
        if (!isTokenValid(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException("Токен недействителен");
        if (userRepository.existsByPhoneNumber(sellerRegistrationRequest.getPhoneNumber()))
            throw new IllegalArgumentException("Номер телефона должен быть уникальным");
        if (kaspiTokenRepository.existsBySellerId(sellerRegistrationRequest.getSellerId()))
            throw new IllegalArgumentException("ID продавца должен быть уникальным.");
        if (kaspiTokenRepository.existsByToken(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException("Токен должен быть уникальным");

        WonderUser wonderUser = sellerMapper.toCreateWonderUser(sellerRegistrationRequest);
        KaspiToken kaspiToken = sellerMapper.toCreateKaspiToken(sellerRegistrationRequest, wonderUser);

        kaspiToken = kaspiTokenRepository.save(kaspiToken);

        wonderUser.setKaspiToken(kaspiToken);

        userRepository.save(wonderUser);

        log.info("Created User with id {}\tCreated Kaspi token with id {}", wonderUser.getId(), kaspiToken.getId());
    }

    @Override
    public WonderUser updateUser(Long id, SellerUserUpdateRequest sellerUserUpdateRequest) {
        final var user = userRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Пользователь с id " + id + " не был найден"));

        sellerMapper.toUpdateUser(user, sellerUserUpdateRequest);

        return userRepository.save(user);
    }

    private boolean isTokenValid(String token) {
        try {
            kaspiApi.getDataCitiesWithToken(token).block();
            return true;
        } catch (Exception e) {
            log.info("Exception: ", e);
            return false;
        }
    }

    private List<KaspiCatalog.Offer> getOffers(List<SupplyAdminResponse.Seller> listOfProducts) {
        return listOfProducts.stream().map(this::mapToOffer).collect(Collectors.toList());
    }


    // todo: refactor the method
    private KaspiCatalog.Offer mapToOffer(SupplyAdminResponse.Seller seller) {
        List<KaspiCatalog.Offer.Availability> availabilities = new ArrayList<>();
        List<KaspiCatalog.Offer.CityPrice> cityPrices = new ArrayList<>();
        KaspiCatalog.Offer offer = new KaspiCatalog.Offer();
        offer.setSku(seller.getProduct().getId().toString());
        offer.setName(seller.getName());

        seller.getProduct().getPrices().forEach(price -> {
            KaspiCatalog.Offer.Availability availability = new KaspiCatalog.Offer.Availability();
            availability.setAvailable((price.getAmount() != null && price.getAmount() != 0) ? "yes" : "no");
            availability.setStoreId(price.getCity().getName());
            availabilities.add(availability);

            KaspiCatalog.Offer.CityPrice cityPrice = new KaspiCatalog.Offer.CityPrice();
            cityPrice.setCityId(price.getCity().getId().toString());
            cityPrice.setPrice(price.getAmount().toString());
            cityPrices.add(cityPrice);
        });

        offer.setAvailabilities(availabilities);
        offer.setCityprices(cityPrices);
        return offer;
    }


    private KaspiCatalog buildKaspiCatalog(List<SupplyAdminResponse.Seller> listOfSellers, KaspiToken kaspiToken) {
        KaspiCatalog kaspiCatalog = new KaspiCatalog();
        kaspiCatalog.setCompany(kaspiToken.getSellerName());
        kaspiCatalog.setMerchantid(kaspiToken.getSellerId());
        kaspiCatalog.setOffers(getOffers(listOfSellers));
        return kaspiCatalog;
    }

    private String marshalObjectToXML(KaspiCatalog kaspiCatalog, Marshaller marshaller) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(kaspiCatalog, writer);
        return writer.toString();
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


    // todo: resolve the issue with selle entity
    @Override
    public String generateOfSellersXmlByKeycloakId(String keycloakId) throws IOException, JAXBException {
        final var listOfSellers = sellerRepository.findAllByKeycloakId(keycloakId);
        final var kaspiToken = kaspiTokenRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                        "Kaspi token doesn't exists",
                        "Create your kaspi token before request"));
        KaspiCatalog kaspiCatalog = buildKaspiCatalog(listOfSellers, kaspiToken);

        log.info("keycloakId: {}, kaspiCatalog: {}", keycloakId, kaspiCatalog);
        Marshaller marshaller = initJAXBContextAndProperties();
        String xmlContent = marshalObjectToXML(kaspiCatalog, marshaller);

        return fileService.save(xmlContent.getBytes(), "xml");
    }
}
