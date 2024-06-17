package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.response.CustomerResponse;
import kz.wonder.wonderuserrepository.entities.Customer;
import kz.wonder.wonderuserrepository.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/kaspi/{kaspiId}")
    public ResponseEntity<CustomerResponse> getCustomerByKaspiId(@PathVariable String kaspiId,
            @RequestBody Customer newCustomer) {
        CustomerResponse customerResponse = customerService.getOrCreateCustomerByKaspiId(kaspiId, newCustomer);
        return ResponseEntity.ok(customerResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id, @RequestBody Customer newCustomer) {
        CustomerResponse customerResponse = customerService.getOrCreateCustomerById(id, newCustomer);
        return ResponseEntity.ok(customerResponse);
    }

    @GetMapping("/kaspi/{kaspiId}/")
    public ResponseEntity<List<CustomerResponse>> getAllCustomersByKaspiId(@PathVariable String kaspiId) {
        return ResponseEntity.ok(customerService.getAllCustomersByKaspiId(kaspiId));
    }
}
