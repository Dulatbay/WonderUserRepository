package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.StoreCellChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.StoreCellMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.StoreCellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreCellServiceImpl implements StoreCellService {
    private final StoreCellRepository storeCellRepository;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    private final UserRepository userRepository;
    private final StoreCellMapper storeCellMapper;

    @Override
    public void create(StoreCellCreateRequest storeCellCreateRequest, String keycloakId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakId, storeCellCreateRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Магазин не существует"));

        StoreCell storeCell = storeCellMapper.toEntity(storeCellCreateRequest);
        storeCell.setKaspiStore(kaspiStore);

        log.info("Created store cell with id: {}", storeCell.getId());

        storeCellRepository.save(storeCell);
    }

    @Override
    public List<StoreCellResponse> getAllByParams(Long storeId, String keycloakId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakId, storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Магазин не существует"));

        return kaspiStore.getStoreCells()
                .stream()
                .map(storeCellMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long cellId, String keycloakId) {
        final var storeCell = storeCellRepository.findById(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Ячейка не существует"));

        if (!storeCell.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakId)) {
            throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Ячейка не существует");
        }
        storeCellRepository.delete(storeCell);
    }

    @Override
    public void addProductToCell(Long cellId, String productArticle, String keycloakId) {
        final var storeCell = storeCellRepository.findById(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Ячейка не существует"));
        final var employee = validateEmployee(keycloakId, storeCell.getKaspiStore().getId());
        final var supplyBoxProduct = validateProduct(productArticle, storeCell.getKaspiStore().getId());

        supplyBoxProduct.setState(ProductStateInStore.ACCEPTED);
        supplyBoxProductsRepository.save(supplyBoxProduct);

        StoreCellProduct storeCellProduct = storeCellMapper.toStoreCellProduct(storeCell, supplyBoxProduct, employee);

        storeCellProductRepository.save(storeCellProduct);
    }

    @Override
    public void moveProductToCell(Long cellId, Long productId, String keycloakId) {

    }

    @Override
    public void deleteProductFromCell(Long cellId, Long productId, String keycloakId) {

    }

    @Override
    public void changeStoreCell(String keycloakId, Long cellId, StoreCellChangeRequest storeCellChangeRequest) {
        final var storeCell = storeCellRepository.findById(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Ячейка не существует"));
        var kaspiStoreOfCell = storeCell.getKaspiStore();

        if(!kaspiStoreOfCell.getWonderUser().getKeycloakId().equals(keycloakId)) {
            throw new IllegalArgumentException("The cell doesn't exist");
        }

        storeCellMapper.updateEntity(storeCell, storeCellChangeRequest);

        storeCellRepository.save(storeCell);
    }

    private StoreEmployee validateEmployee(String keycloakId, Long kaspiStoreId) {
        var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Сотрудник не существует"));

        var employeeWorkStore = employee.getKaspiStore();

        log.info("Employee work store: {}, requested store: {}", employeeWorkStore.getId(), kaspiStoreId);


        if (!employeeWorkStore.getId().equals(kaspiStoreId)) {
            throw new IllegalArgumentException("У вас нет разрешения добавлять товар в ячейку этого магазина.");
        }
        return employee;
    }

    private SupplyBoxProduct validateProduct(String article, Long kaspiStoreId) {
        final var supplyBoxProduct = supplyBoxProductsRepository.findByArticle(article)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

        validateProductForStoreCell(supplyBoxProduct, kaspiStoreId);
        return supplyBoxProduct;
    }

    private void validateProductForStoreCell(SupplyBoxProduct supplyBoxProduct, Long kaspiStoreId) {
        if (!supplyBoxProduct.getSupplyBox().getSupply().getKaspiStore().getId().equals(kaspiStoreId)) {
            throw new IllegalArgumentException("У вас нет разрешения на добавление этого товара в ячейку.");
        }
        if (supplyBoxProduct.getState() == ProductStateInStore.SOLD)
            throw new IllegalArgumentException("Товар уже продан");
    }
}
