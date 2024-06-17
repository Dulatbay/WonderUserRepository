package kz.wonder.wonderuserrepository.dto.response;

import jakarta.persistence.Column;
import kz.wonder.wonderuserrepository.entities.Customer;
import lombok.Data;

@Data
public class CustomerResponse {
    private String kaspiId;
    private String name;
    private String cellPhone;
    private String firstName;
    private String lastName;

    public CustomerResponse(Customer customer) {
        this.kaspiId = customer.getKaspiId();
        this.name = customer.getName();
        this.cellPhone = customer.getCellPhone();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
    }
}
