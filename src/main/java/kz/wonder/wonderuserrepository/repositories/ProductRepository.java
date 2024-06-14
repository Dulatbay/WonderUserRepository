package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByVendorCodeAndKeycloakIdAndDeletedIsFalse(String vendorCode, String keycloakId);

    Optional<Product> findByOriginalVendorCodeAndKeycloakIdAndDeletedIsFalse(String originalVendorCode, String keycloakId);

    Optional<Product> findByIdAndKeycloakIdAndDeletedIsFalse(Long id, String keycloakId);

    @Query("SELECT p FROM Product p WHERE " +
            "((:name IS NULL OR lower(p.name) LIKE '%' || lower(:name) || '%') OR " +
            "(:vendorCode IS NULL OR lower(p.vendorCode) LIKE '%' || lower(:vendorCode) || '%')) AND " +
            "(:isEnabled IS NULL OR p.enabled = :isEnabled) AND " +
            "p.deleted = false")
    Page<Product> findAllBy(@Param("name") String name,
                            @Param("vendorCode") String vendorCode,
                            @Param("isEnabled") Boolean isEnabled,
                            Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.keycloakId = :keycloakUserId AND " +
            "((:name IS NULL OR lower(p.name) LIKE '%' || lower(:name) || '%') OR " +
            "(:vendorCode IS NULL OR lower(p.vendorCode) LIKE '%' || lower(:vendorCode) || '%')) AND " +
            "(:isEnabled IS NULL OR p.enabled = :isEnabled) AND " +
            "p.deleted = false")
    Page<Product> findByParams(
            @Param("keycloakUserId") String keycloakUserId,
            @Param("name") String name,
            @Param("vendorCode") String vendorCode,
            @Param("isEnabled") Boolean isEnabled,
            Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.keycloakId = :keycloakUserId AND p.deleted = false")
    List<Product> findAllSellerProductsWithPrices(String keycloakUserId);

    @Query("SELECT p FROM Product p " +
            "WHERE p.keycloakId = :keycloakUserId " +
            "AND ((:name IS NULL OR lower(p.name) LIKE '%' || lower(:name)|| '%') OR (:vendorCode IS NULL OR lower(p.vendorCode) LIKE '%' || lower(:vendorCode) || '%')) " +
            "AND (:isEnabled IS NULL OR p.enabled = :isEnabled) " +
            "AND p.deleted = false")
    Page<Product> findAllByKeycloakId(@Param("keycloakUserId") String keycloakUserId,
                                      @Param("name") String name,
                                      @Param("vendorCode") String vendorCode,
                                      @Param("isEnabled") Boolean isEnabled,
                                      Pageable pageable);

    boolean existsByOriginalVendorCodeAndDeletedIsFalse(String originalVendorCode);
}
