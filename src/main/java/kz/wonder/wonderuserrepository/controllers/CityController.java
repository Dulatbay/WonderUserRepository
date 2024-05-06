package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.PaginatedResponse;
import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cities")
public class CityController {
    private final CityService cityService;

    @GetMapping()
    public ResponseEntity<List<CityResponse>> getCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync() {
        cityService.syncWithKaspi();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
