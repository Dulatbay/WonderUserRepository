package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/box-types")
public class BoxTypesController {
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(@ModelAttribute BoxTypeCreateRequest boxTypeCreateRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
