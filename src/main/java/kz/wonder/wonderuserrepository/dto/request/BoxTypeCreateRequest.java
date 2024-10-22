package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class BoxTypeCreateRequest {
    @NotNull(message = "{requests.box-type-create-request.please-provide-a-name}")
    private String name;

    @NotNull(message = "{requests.box-type-create-request.please-provide-a-description}")
    private String description;

    private List<MultipartFile> images;
}
