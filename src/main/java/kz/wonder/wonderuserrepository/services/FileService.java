package kz.wonder.wonderuserrepository.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String save(MultipartFile file);

    Resource loadAsResource(String fileName);

    void init();

    void deleteByName(String url);

    void deleteAll();
}
