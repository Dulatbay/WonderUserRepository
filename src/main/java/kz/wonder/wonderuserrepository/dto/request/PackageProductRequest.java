package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PackageProductRequest {
    private List<String> productArticles;
}
