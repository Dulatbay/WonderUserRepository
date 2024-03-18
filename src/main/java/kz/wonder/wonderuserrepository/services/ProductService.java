package kz.wonder.wonderuserrepository.services;

import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    void processExcelFile(MultipartFile file);
}
