package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.response.CustomerResponse;
import kz.wonder.wonderuserrepository.entities.Customer;
import kz.wonder.wonderuserrepository.repositories.CustomerRepository;
import kz.wonder.wonderuserrepository.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse getOrCreateCustomerByKaspiId(String kaspiId, Customer newCustomer) {
        Optional<Customer> existingCustomer = customerRepository.findCustomerByKaspiId(kaspiId);
        Customer customer;
        if (existingCustomer.isPresent()) {
            customer = existingCustomer.get();
        } else {
            newCustomer.setKaspiId(kaspiId);
            customer = customerRepository.save(newCustomer);
        }
        return new CustomerResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse getOrCreateCustomerById(Long id, Customer newCustomer) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        Customer customer;
        if (existingCustomer.isPresent()) {
            customer = existingCustomer.get();
        } else {
            customer = customerRepository.save(newCustomer);
        }

        return new CustomerResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomersByKaspiId(String kaspiId) {
        List<Customer> customers = customerRepository.findAllByKaspiId(kaspiId);
        return customers.stream()
                .map(CustomerResponse::new)
                .toList();
    }
}
