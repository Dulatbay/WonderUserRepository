package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdmins;
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
    @Operation(summary = "Create new box type", description = "This endpoint allows to create new box types with given name and description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new box type")
    })
    @AccessForAdmins
    public ResponseEntity<Void> create(@ModelAttribute @Valid BoxTypeCreateRequest boxTypeCreateRequest) {
        boxTypeService.createBoxType(boxTypeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping()
    @Operation(summary = "Get all box types of the store", description = "Retrieves all box types of the store. Start by writing id of the required store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all box types for the store")
    })
    public ResponseEntity<List<BoxTypeResponse>> getAllByStoreId(@Parameter(description = "ID of the store to get box types", required = true)
                                                                 @RequestParam(value = "store-id", required = false) Long storeId) {
        return ResponseEntity.ok(boxTypeService.getAllByStore(storeId));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all box types", description = "Retrieves all box types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all box types")
    })
    public ResponseEntity<List<BoxTypeResponse>> getAll() {
        return ResponseEntity.ok(boxTypeService.getAll());
    }


    @DeleteMapping("{id}")
    @Operation(summary = "Delete existing box type", description = "This endpoint allows to delete existing box type. Fill in the id of the box type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the box type by ID")
    })
    @AccessForAdmins
    public ResponseEntity<Void> delete(@Parameter(description = "ID of the box type to be deleted", required = true)
                                       @PathVariable Long id) {
        boxTypeService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
