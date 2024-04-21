package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.StoreCellService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreCellServiceImpl implements StoreCellService {
    private final StoreCellRepository storeCellRepository;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final StoreCellProductRepository storeCellProductRepository;

    @Override
    public void create(StoreCellCreateRequest storeCellCreateRequest, String keycloakId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakId, storeCellCreateRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

        StoreCell storeCell = new StoreCell();
        storeCell.setCell(storeCellCreateRequest.getCell());
        storeCell.setCol(storeCellCreateRequest.getCol());
        storeCell.setRow(storeCellCreateRequest.getRow());
        storeCell.setComment(storeCellCreateRequest.getComment());
        storeCell.setWidth(storeCellCreateRequest.getWidth());
        storeCell.setHeight(storeCellCreateRequest.getHeight());
        storeCell.setKaspiStore(kaspiStore);
        storeCellRepository.save(storeCell);
    }

    @Override
    public List<StoreCellResponse> getAllByParams(Long storeId, String keycloakId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakId, storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

        return kaspiStore.getStoreCells()
                .stream()
                .map(cell -> {
                    StoreCellResponse storeCell = new StoreCellResponse();
                    storeCell.setCell(cell.getCell());
                    storeCell.setCol(cell.getCol());
                    storeCell.setRow(cell.getRow());
                    storeCell.setComment(cell.getComment());
                    storeCell.setWidth(cell.getWidth());
                    storeCell.setHeight(cell.getHeight());
                    storeCell.setId(cell.getId());
                    return storeCell;
                })
                .toList();
    }

    @Override
    public void delete(Long cellId, String keycloakId) {
        final var storeCell = storeCellRepository.findById(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Cell doesn't exist"));

        if (!storeCell.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakId)) {
            throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Store doesn't exist");
        }
        storeCellRepository.delete(storeCell);
    }

    @Override
    public void addProductToCell(Long cellId, Long productId, String keycloakId) {
        final var storeCell = storeCellRepository.findById(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Cell doesn't exist"));
        final var employee = validateEmployee(keycloakId, storeCell.getKaspiStore().getId());
        final var supplyBoxProduct = validateProduct(productId, storeCell.getKaspiStore().getId());

        supplyBoxProduct.setState(ProductStateInStore.ACCEPTED);
        supplyBoxProductsRepository.save(supplyBoxProduct);

        StoreCellProduct storeCellProduct = new StoreCellProduct();
        storeCellProduct.setStoreCell(storeCell);
        storeCellProduct.setSupplyBoxProduct(supplyBoxProduct);
        storeCellProduct.setStoreEmployee(employee);
        storeCellProductRepository.save(storeCellProduct);
    }

    @Override
    public void moveProductToCell(Long cellId, Long productId, String keycloakId) {

    }

    @Override
    public void deleteProductFromCell(Long cellId, Long productId, String keycloakId) {

    }

    private StoreEmployee validateEmployee(String keycloakId, Long kaspiStoreId) {
        var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Employee doesn't exist"));

        var employeeWorkStore = employee.getKaspiStore();

        if (!employeeWorkStore.getId().equals(kaspiStoreId)) {
            throw new IllegalArgumentException("You have not permission to add product to cell of this store");
        }
        return employee;
    }
    private SupplyBoxProduct validateProduct(Long productId, Long kaspiStoreId) {
        final var supplyBoxProduct = supplyBoxProductsRepository.findByProductId(productId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));

        validateProductForStoreCell(supplyBoxProduct, kaspiStoreId);
        return supplyBoxProduct;
    }
    private void validateProductForStoreCell(SupplyBoxProduct supplyBoxProduct, Long kaspiStoreId) {
        if (!supplyBoxProduct.getSupplyBox().getSupply().getKaspiStore().getId().equals(kaspiStoreId)) {
            throw new IllegalArgumentException("You have not permission to add this product to cell");
        }
        if (supplyBoxProduct.getState() == ProductStateInStore.SOLD)
            throw new IllegalArgumentException("Product is already sold");
    }
}
