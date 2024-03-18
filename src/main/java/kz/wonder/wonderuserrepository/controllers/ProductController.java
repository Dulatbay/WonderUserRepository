package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.response.MessageResponse;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping("/by-file")
    private ResponseEntity<MessageResponse> createByFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        productService.processExcelFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Products upload successfully"));
    }

}
