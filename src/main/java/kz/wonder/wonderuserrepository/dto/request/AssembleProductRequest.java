package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssembleProductRequest {
    private List<String> productArticles;
}
