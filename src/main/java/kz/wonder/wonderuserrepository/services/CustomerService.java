package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.CustomerResponse;
import kz.wonder.wonderuserrepository.entities.Customer;

import java.util.List;

public interface CustomerService {
    CustomerResponse getOrCreateCustomerByKaspiId(String kaspiId, Customer newCustomer);

    CustomerResponse getOrCreateCustomerById(Long id, Customer newCustomer);

    List<CustomerResponse> getAllCustomersByKaspiId(String kaspiId);
}
