package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class BoxTypeCreateRequest {
    private String name;
    private String description;
    private List<MultipartFile> images;
}
