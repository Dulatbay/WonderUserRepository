package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.enums.ApplicationMode;
import kz.wonder.wonderuserrepository.entities.ApplicationProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationPropertyRepository extends JpaRepository<ApplicationProperty, Long> {
    Optional<ApplicationProperty> findByPropertyNameAndApplicationMode(String propertyName, ApplicationMode applicationMode);
}
