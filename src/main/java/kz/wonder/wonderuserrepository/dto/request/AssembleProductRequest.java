package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssembleProductRequest {
    @NotNull(message = "Введите номер заказа")
    private String orderCode;
    private List<String> productArticles;
}
