package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findCustomerByKaspiId(String kaspiId);
    List<Customer> findAllByKaspiId(String kaspiId);
}
