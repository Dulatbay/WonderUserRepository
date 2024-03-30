package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/products")
public class ProductController {
	private final ProductService productService;

	@PostMapping(name = "/by-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<List<ProductResponse>> createByFile(@RequestPart("file") MultipartFile file) {
		var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		var userId = Utils.extractIdFromToken(token);
		List<ProductResponse> result = productService.processExcelFile(file, userId);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(result);
	}

	@GetMapping()
	public ResponseEntity<List<ProductResponse>> getProducts() {
		var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

		return ResponseEntity.ok(productService.getProductsByKeycloakId(Utils.extractIdFromToken(token)));
	}


}
