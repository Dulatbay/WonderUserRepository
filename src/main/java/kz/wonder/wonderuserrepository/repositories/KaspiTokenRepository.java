package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KaspiTokenRepository extends JpaRepository<KaspiToken, Long> {
    boolean existsBySellerId(String sellerId);
    boolean existsByToken(String token);

    Optional<KaspiToken> findByWonderUserKeycloakId(String keycloakId);

    @Query("SELECT t FROM KaspiToken t " +
            "JOIN FETCH t.wonderUser")
    List<KaspiToken> findAllWithFetching();

    @Query("select kt FROM KaspiToken kt " +
            "LEFT JOIN WonderUser wu ON wu.id = kt.wonderUser.id " +
            "LEFT JOIN Supply s ON s.author.id = wu.id AND s.kaspiStore.id = :storeId " +
            "LEFT JOIN SupplyBox sb ON sb.supply.id = s.id " +
            "LEFT JOIN SupplyBoxProduct sbp ON sbp.supplyBox.id = sb.id AND (sbp.state != 'SOLD' OR sbp.state != 'DECLINED')")
    List<KaspiToken> findAllSellersInStoreWithProducts(Long storeId);


    @Query("SELECT kt FROM KaspiToken kt " +
            "LEFT JOIN FETCH kt.wonderUser " +
            "WHERE kt.xmlUpdated = false")
    List<KaspiToken> findAllXmlsToUpdate();
}
