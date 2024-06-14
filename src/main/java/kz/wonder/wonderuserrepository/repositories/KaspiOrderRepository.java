package kz.wonder.wonderuserrepository.repositories;


import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
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
            "WHERE ko.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode is null OR ko.deliveryMode = :deliveryMode) " +
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
            "WHERE ko.kaspiStore.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode is null OR ko.deliveryMode = :deliveryMode) " +
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
            "WHERE w.keycloakId = :keycloakId AND ko.creationDate BETWEEN :from AND :to")
    List<KaspiOrder> findAllSellerOrders(String keycloakId, Long from, Long to);

    @Query("select ko from KaspiOrder ko WHERE ko.kaspiStore.wonderUser.keycloakId = :keycloakId AND ko.creationDate BETWEEN :from AND :to ORDER BY ko.creationDate ASC")
    List<KaspiOrder> findAllAdminOrders(String keycloakId, Long from, Long to);

    @Query("SELECT ko FROM KaspiOrder ko " +
            "RIGHT JOIN KaspiOrderProduct kop ON kop.order.id = ko.id " +
            "LEFT JOIN StoreEmployee se ON se.kaspiStore.id = ko.kaspiStore.id " +
            "WHERE se.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode IS NULL OR ko.deliveryMode = :deliveryMode) " +
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
                                           String searchValue,
                                           boolean byOrderCode,
                                           boolean byShopName,
                                           boolean byStoreAddress,
                                           boolean byProductName,
                                           boolean byProductArticle,
                                           boolean byProductVendorCode,
                                           Pageable pageable);





}
