package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.kaspi.client.model.response.Order.OrderEntry;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.KaspiOrderProduct;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.enums.KaspiProductUnitType;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.KaspiDeliveryAddressMapper;
import kz.wonder.wonderuserrepository.mappers.KaspiOrderMapper;
import kz.wonder.wonderuserrepository.mappers.KaspiStoreMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.ApplicationPropertyService;
import kz.wonder.wonderuserrepository.services.KaspiProductCategoryService;
import kz.wonder.wonderuserrepository.services.OrderParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.UPDATE_ORDERS_IGNORE_TIME_PROPERTY_NAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderParseServiceImpl implements OrderParseService {
    private final KaspiOrderRepository kaspiOrderRepository;
    private final KaspiOrderMapper kaspiOrderMapper;
    private final KaspiDeliveryAddressMapper kaspiDeliveryAddressMapper;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreMapper kaspiStoreMapper;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final ProductRepository productRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final KaspiApi kaspiApi;
    private final ApplicationPropertyService applicationPropertyService;
    private final KaspiProductCategoryService kaspiProductCategoryService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processKaspiOrder(KaspiToken token, OrdersDataResponse.OrdersDataItem order, List<OrderEntry> products) {
        var kaspiOrder = saveKaspiOrder(order, token);
        boolean storeNotFound = (kaspiOrder.getKaspiCity() == null || kaspiOrder.getKaspiStore() == null);

        var orderEntries = products.stream().filter(p -> p.getId().startsWith(order.getOrderId()) && !kaspiOrderProductRepository.existsByKaspiId(p.getId())).toList();

        for (var orderEntry : orderEntries) {
            processOrderProduct(token, kaspiOrder, orderEntry);

            if (storeNotFound) {
                var pointOfServiceResponse = kaspiApi.getStoreById(orderEntry.getId(), token.getToken()).block();

                assert pointOfServiceResponse != null;
                var kaspiCity = kaspiCityRepository.findByKaspiId(pointOfServiceResponse.getCityRelationship().getData().getId())
                        .orElseThrow(() -> new RuntimeException("Kaspi Store not found"));

                var kaspiStore = kaspiStoreMapper.findByAddress(pointOfServiceResponse.getAddress(), kaspiCity);

                if (kaspiStore.isEmpty())
                    kaspiStore = kaspiStoreRepository.findByOriginAddressId(pointOfServiceResponse.getId());

                if (kaspiStore.isEmpty()) {
                    log.info("Create store with kaspiEndPoint: {}", kaspiOrder.getCode());

                    var createdKaspiStore = kaspiStoreMapper.createStoreByParamsOfOrder(pointOfServiceResponse.getId(),
                            pointOfServiceResponse.getDisplayName(),
                            pointOfServiceResponse.getAddress(),
                            kaspiCity,
                            null
                    );

                    kaspiOrder.setKaspiStore(createdKaspiStore);
                    kaspiOrderRepository.save(kaspiOrder);
                } else {
                    kaspiOrder.setKaspiStore(kaspiStore.get());
                    kaspiOrderRepository.save(kaspiOrder);
                }

                storeNotFound = false;
            }

        }
    }

    private void processOrderProduct(KaspiToken token, KaspiOrder kaspiOrder, OrderEntry orderEntry) {
        var vendorCode = kaspiOrderMapper.extractVendorCode(orderEntry);

        var product = productRepository
                .findByOriginalVendorCodeAndKeycloakIdAndDeletedIsFalse(vendorCode,
                        token.getWonderUser().getKeycloakId())
                .orElse(null);


        if (product != null) {

            var isAlreadyBought = supplyBoxProductsRepository.existsByKaspiOrderCode(kaspiOrder.getCode());

            if(isAlreadyBought)
                return;

            var supplyBoxProductOptional = supplyBoxProductsRepository.findFirstByStoreIdAndProductIdAndState(kaspiOrder.getKaspiStore().getId(), product.getId(), ProductStateInStore.ACCEPTED);


            if (supplyBoxProductOptional.isPresent()) {
                var supplyBoxProduct = supplyBoxProductOptional.get();
                var sellAt = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());


                log.info("accepted time: {}, now: {}", supplyBoxProduct.getAcceptedTime(), sellAt);

                if (applicationPropertyService.getApplicationPropertyByName(UPDATE_ORDERS_IGNORE_TIME_PROPERTY_NAME).getValue().equals("true")
                        || (supplyBoxProduct.getAcceptedTime() != null && supplyBoxProduct.getAcceptedTime().isBefore(sellAt))) {
                    log.info("supplyBoxProduct to save: {}", supplyBoxProduct.getId());
                }

                supplyBoxProduct.setState(ProductStateInStore.WAITING_FOR_ASSEMBLY);
                supplyBoxProduct.setKaspiOrder(kaspiOrder);
                supplyBoxProductsRepository.save(supplyBoxProduct);
                log.info("SOLD MENTIONED, product id: {}, order code: {}", product.getId(), kaspiOrder.getCode());

                KaspiOrderProduct kaspiOrderProduct = new KaspiOrderProduct();
                kaspiOrderProduct.setOrder(kaspiOrder);
                kaspiOrderProduct.setProduct(product);
                kaspiOrderProduct.setKaspiId(orderEntry.getId());
                kaspiOrderProduct.setQuantity(orderEntry.getAttributes().getQuantity());
                kaspiOrderProduct.setCategory(kaspiProductCategoryService.findOrCreate(orderEntry.getAttributes().getCategory().getCode(), orderEntry.getAttributes().getCategory().getTitle()));
                kaspiOrderProduct.setBasePrice(orderEntry.getAttributes().getBasePrice());
                kaspiOrderProduct.setDeliveryCost(orderEntry.getAttributes().getDeliveryCost());
                kaspiOrderProduct.setEntryNumber(orderEntry.getAttributes().getEntryNumber());
                kaspiOrderProduct.setUnitType(KaspiProductUnitType.getByDescription(orderEntry.getAttributes().getUnitType()));
                kaspiOrderProduct.setWeight(orderEntry.getAttributes().getWeight());
                kaspiOrderProduct.setSupplyBoxProduct(supplyBoxProduct);

                kaspiOrderProductRepository.save(kaspiOrderProduct);
            }


        }

        kaspiOrderRepository.save(kaspiOrder);
    }


    private KaspiOrder saveKaspiOrder(OrdersDataResponse.OrdersDataItem order, KaspiToken token) {
        var orderAttributes = order.getAttributes();
        var optionalKaspiOrder = kaspiOrderRepository.findByCode(orderAttributes.getCode());

        if (optionalKaspiOrder.isPresent()) {
            return kaspiOrderMapper.updateKaspiOrder(optionalKaspiOrder.get(), token, order, orderAttributes);
        } else {
            var kaspiOrder = kaspiOrderMapper.saveKaspiOrder(token, order, orderAttributes);

            detectStoreAndCity(orderAttributes, kaspiOrder);

            return kaspiOrderRepository.save(kaspiOrder);
        }
    }

    private void detectStoreAndCity(OrdersDataResponse.OrderAttributes orderAttributes, KaspiOrder kaspiOrder) {
        if (orderAttributes.getDeliveryAddress() != null) {
            kaspiOrder.setDeliveryAddress(kaspiDeliveryAddressMapper.getKaspiDeliveryAddress(orderAttributes));
        }

        // if the originAddress is null, then an order delivery type is pickup
        if (orderAttributes.getOriginAddress() != null) {

            var kaspiCity = kaspiCityRepository.findByCode(orderAttributes.getOriginAddress().getCity().getCode())
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Kaspi city not found", ""));

            var kaspiStore = kaspiStoreMapper.getKaspiStore(orderAttributes, orderAttributes.getOriginAddress(), kaspiCity);

            kaspiOrder.setKaspiStore(kaspiStore);
            kaspiOrder.setKaspiCity(kaspiCity);
        } else {
            var pickupPointId = orderAttributes.getPickupPointId();

            var kaspiStoreOptional = kaspiStoreRepository.findByPickupPointId(pickupPointId);

            if (kaspiStoreOptional.isPresent()) {
                kaspiOrder.setKaspiStore(kaspiStoreOptional.get());
                kaspiOrder.setKaspiCity(kaspiStoreOptional.get().getKaspiCity());
            }
        }
    }


}
