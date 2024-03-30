package kz.wonder.wonderuserrepository.controllers;

import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;
import kz.wonder.wonderuserrepository.services.BoxTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/box-types")
public class BoxTypesController {

	private final BoxTypeService boxTypeService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> create(@ModelAttribute @Valid BoxTypeCreateRequest boxTypeCreateRequest) {
		boxTypeService.createBoxType(boxTypeCreateRequest);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping
	public ResponseEntity<List<BoxTypeResponse>> getAll() {
		return ResponseEntity.ok(boxTypeService.getAll());
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		boxTypeService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
