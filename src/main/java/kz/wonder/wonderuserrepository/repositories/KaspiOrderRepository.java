package kz.wonder.wonderuserrepository.repositories;


import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KaspiOrderRepository extends JpaRepository<KaspiOrder, Long> {
    Optional<KaspiOrder> findByCode(String code);

    @Query("select ko from KaspiOrder ko " +
            "RIGHT JOIN KaspiOrderProduct kop ON kop.order.id = ko.id " +
            "LEFT JOIN StoreEmployee se ON se.kaspiStore.id = ko.kaspiStore.id " +
            "LEFT JOIN OrderAssemble oa ON oa.kaspiOrder.id = ko.id " +
            "LEFT JOIN OrderPackage op ON op.kaspiOrder.id = ko.id " +
            "LEFT JOIN OrderTransmission ot ON ot.kaspiOrder.id = ko.id " +
            "WHERE ko.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode is null OR ko.deliveryMode = :deliveryMode) " +
            "AND (:orderBaseStatus IS NULL " +
            "OR (:orderBaseStatus = 'ASSEMBLING' AND (oa is null OR (oa.assembleState != 'FINISHED'))) " +
            "OR (:orderBaseStatus = 'PACKAGING' AND (op.packageState != 'FINISHED')) " +
            "OR (:orderBaseStatus = 'SENDING' AND (ot.orderTransmissionState != 'FINISHED')) " +
            ")" +
            "AND (" +
            "(:byOrderCode = FALSE AND :byShopName = FALSE AND :byStoreAddress = FALSE AND :byProductName = FALSE AND :byProductArticle = FALSE AND :byProductVendorCode = FALSE) " +
            "OR " +
            "(:byOrderCode = TRUE AND LOWER(ko.code) LIKE '%' || :searchValue || '%') " +
            "OR (:byShopName = TRUE AND LOWER(ko.wonderUser.kaspiToken.sellerName) LIKE '%' || :searchValue || '%') " +
            "OR (:byStoreAddress = TRUE AND LOWER(ko.kaspiStore.formattedAddress) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductName = TRUE AND LOWER(kop.product.name) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductArticle = TRUE AND LOWER(kop.supplyBoxProduct.article) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductVendorCode = TRUE AND :searchValue LIKE kop.product.vendorCode)) ")
    Page<KaspiOrder> findAllSellerOrders(String keycloakId,
                                         Long from,
                                         Long to,
                                         DeliveryMode deliveryMode,
                                         String orderBaseStatus,
                                         String searchValue,
                                         boolean byOrderCode,
                                         boolean byShopName,
                                         boolean byStoreAddress,
                                         boolean byProductName,
                                         boolean byProductArticle,
                                         boolean byProductVendorCode,
                                         Pageable pageable);

    @Query("select ko from KaspiOrder ko " +
            "RIGHT JOIN KaspiOrderProduct kop ON kop.order.id = ko.id " +
            "LEFT JOIN StoreEmployee se ON se.kaspiStore.id = ko.kaspiStore.id " +
            "LEFT JOIN OrderAssemble oa ON oa.kaspiOrder.id = ko.id " +
            "LEFT JOIN OrderPackage op ON op.kaspiOrder.id = ko.id " +
            "LEFT JOIN OrderTransmission ot ON ot.kaspiOrder.id = ko.id " +
            "WHERE ko.kaspiStore.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode is null OR ko.deliveryMode = :deliveryMode) " +
            "AND (:orderBaseStatus IS NULL " +
            "OR (:orderBaseStatus = 'ASSEMBLING' AND (oa is null OR (oa.assembleState != 'FINISHED'))) " +
            "OR (:orderBaseStatus = 'PACKAGING' AND (op.packageState != 'FINISHED')) " +
            "OR (:orderBaseStatus = 'SENDING' AND (ot.orderTransmissionState != 'FINISHED')) " +
            ")" +
            "AND (" +
            "(:byOrderCode = FALSE AND :byShopName = FALSE AND :byStoreAddress = FALSE AND :byProductName = FALSE AND :byProductArticle = FALSE AND :byProductVendorCode = FALSE) " +
            "OR " +
            "(:byOrderCode = TRUE AND LOWER(ko.code) LIKE '%' || :searchValue || '%') " +
            "OR (:byShopName = TRUE AND LOWER(ko.wonderUser.kaspiToken.sellerName) LIKE '%' || :searchValue || '%') " +
            "OR (:byStoreAddress = TRUE AND LOWER(ko.kaspiStore.formattedAddress) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductName = TRUE AND LOWER(kop.product.name) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductArticle = TRUE AND LOWER(kop.supplyBoxProduct.article) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductVendorCode = TRUE AND :searchValue LIKE kop.product.vendorCode)) " +
            "ORDER BY ko.creationDate ASC")
    Page<KaspiOrder> findAllAdminOrders(String keycloakId,
                                        Long from,
                                        Long to,
                                        DeliveryMode deliveryMode,
                                        String orderBaseStatus,
                                        String searchValue,
                                        boolean byOrderCode,
                                        boolean byShopName,
                                        boolean byStoreAddress,
                                        boolean byProductName,
                                        boolean byProductArticle,
                                        boolean byProductVendorCode,
                                        Pageable pageable);

    @Query("select ko from KaspiOrder ko " +
            "RIGHT JOIN KaspiOrderProduct kop ON kop.order.id = ko.id " +
            "LEFT JOIN WonderUser w ON w.id = ko.wonderUser.id " +
            "WHERE w.keycloakId = :keycloakId AND (ko.creationDate BETWEEN :from AND :to)")
    List<KaspiOrder> findAllSellerOrders(String keycloakId, Long from, Long to);

    @Query("SELECT ko FROM KaspiOrder ko " +
            "RIGHT JOIN KaspiOrderProduct kop ON kop.order.id = ko.id " +
            "LEFT JOIN FETCH ko.products " +
            "LEFT JOIN FETCH ko.kaspiStore ks " +
            "LEFT JOIN FETCH ks.wonderUser wu " +
            "LEFT JOIN FETCH ko.orderAssemble oa " +
            "LEFT JOIN FETCH ko.orderPackage op " +
            "LEFT JOIN FETCH ko.orderTransmission ot " +
            "WHERE wu.keycloakId = :keycloakId AND ko.creationDate BETWEEN :startDate AND :endDate " +
            "ORDER BY ko.creationDate")
    List<KaspiOrder> findAllAdminOrders(String keycloakId, Long startDate, Long endDate);

    @Query("SELECT ko FROM KaspiOrder ko " +
            "RIGHT JOIN KaspiOrderProduct kop ON kop.order.id = ko.id " +
            "LEFT JOIN StoreEmployee se ON se.kaspiStore.id = ko.kaspiStore.id " +
            "LEFT JOIN OrderAssemble oa ON oa.kaspiOrder.id = ko.id " +
            "LEFT JOIN OrderPackage op ON op.kaspiOrder.id = ko.id " +
            "LEFT JOIN OrderTransmission ot ON ot.kaspiOrder.id = ko.id " +
            "WHERE se.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode IS NULL OR ko.deliveryMode = :deliveryMode) " +
            "AND (:orderBaseStatus IS NULL " +
            "OR (:orderBaseStatus = 'ASSEMBLING' AND (oa is null OR (oa.assembleState != 'FINISHED'))) " +
            "OR (:orderBaseStatus = 'PACKAGING' AND (op.packageState != 'FINISHED')) " +
            "OR (:orderBaseStatus = 'SENDING' AND (ot.orderTransmissionState != 'FINISHED')) " +
            ")" +
            "AND (" +
            "(:byOrderCode = FALSE AND :byShopName = FALSE AND :byStoreAddress = FALSE AND :byProductName = FALSE AND :byProductArticle = FALSE AND :byProductVendorCode = FALSE) " +
            "OR " +
            "(:byOrderCode = TRUE AND LOWER(ko.code) LIKE '%' || :searchValue || '%') " +
            "OR (:byShopName = TRUE AND LOWER(ko.wonderUser.kaspiToken.sellerName) LIKE '%' || :searchValue || '%') " +
            "OR (:byStoreAddress = TRUE AND LOWER(ko.kaspiStore.formattedAddress) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductName = TRUE AND LOWER(kop.product.name) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductArticle = TRUE AND LOWER(kop.supplyBoxProduct.article) LIKE '%' || :searchValue || '%') " +
            "OR (:byProductVendorCode = TRUE AND :searchValue LIKE kop.product.vendorCode)) " +
            "ORDER BY ko.creationDate ASC")
    Page<KaspiOrder> findAllEmployeeOrders(String keycloakId,
                                           Long from,
                                           Long to,
                                           DeliveryMode deliveryMode,
                                           String orderBaseStatus,
                                           String searchValue,
                                           boolean byOrderCode,
                                           boolean byShopName,
                                           boolean byStoreAddress,
                                           boolean byProductName,
                                           boolean byProductArticle,
                                           boolean byProductVendorCode,
                                           Pageable pageable);


}
