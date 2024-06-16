package kz.wonder.wonderuserrepository.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-transmission")
public class OrderTransmissionController {

    @PostMapping("/{orderCode}/transfer")
    public ResponseEntity<Void> transferOrder(@PathVariable String orderCode) {

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
