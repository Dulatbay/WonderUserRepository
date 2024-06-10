package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.dto.response.SupplyAdminResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerRepository extends JpaRepository<SupplyAdminResponse.Seller, Long> {
    List<SupplyAdminResponse.Seller findAllByKeycloakId(String keycloakId);
}
