package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.SupplyAdminResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyProcessFileResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyProductResponse;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.SupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/supplies")
public class SupplyController {

	private final SupplyService supplyService;
	private final KeycloakService keycloakService;

	@PostMapping(value = "/process-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<List<SupplyProcessFileResponse>> processFile(@RequestPart("file") MultipartFile file) {
		var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		var userId = Utils.extractIdFromToken(token);
		var result = supplyService.processFile(file, userId);
		return ResponseEntity.ok(result);
	}

	@PostMapping
	public ResponseEntity<Void> createSupply(@RequestBody SupplyCreateRequest createRequest) {
		var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		var userId = Utils.extractIdFromToken(token);
		supplyService.createSupply(createRequest, userId);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/admin")
	public ResponseEntity<List<SupplyAdminResponse>> getSuppliesAdmin(@RequestParam("start-date") LocalDate startDate,
	                                                                  @RequestParam("end-date") LocalDate endDate
	) {
		var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		var id = Utils.extractIdFromToken(token);
		var userRepresentation = keycloakService.getUserById(id).toRepresentation();

		List<SupplyAdminResponse> result = supplyService.getSuppliesOfAdmin(startDate, endDate, userRepresentation.getId(), userRepresentation.getFirstName() + " " + userRepresentation.getLastName());
		return ResponseEntity.ok(result);
	}

	@GetMapping("/admin/detail/{id}")
	public ResponseEntity<List<SupplyProductResponse>> getSuppliesDetail(@PathVariable("id") Long id){
		List<SupplyProductResponse> result = supplyService.getSuppliesDetail(id);
		return ResponseEntity.ok(result);
	}
}
