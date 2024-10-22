package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.StoreCellChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.StoreCellMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.StoreCellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final StoreCellMapper storeCellMapper;
    private final MessageSource messageSource;

    @Override
    public void create(StoreCellCreateRequest storeCellCreateRequest, String keycloakId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndIdAndDeletedIsFalse(keycloakId, storeCellCreateRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.store-not-found", null, LocaleContextHolder.getLocale())));

        StoreCell storeCell = storeCellMapper.toEntity(storeCellCreateRequest);
        storeCell.setKaspiStore(kaspiStore);
        storeCell.setDeleted(false);

        log.info("Created store cell with id: {}", storeCell.getId());

        storeCellRepository.save(storeCell);
    }

    @Override
    public List<StoreCellResponse> getAllByParams(Long storeId, String keycloakId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndIdAndDeletedIsFalse(keycloakId, storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.store-not-found", null, LocaleContextHolder.getLocale())));

        return kaspiStore.getStoreCells()
                .stream()
                .filter(s -> !s.isDeleted())
                .map(storeCellMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long cellId, String keycloakId) {
        final var storeCell = storeCellRepository.findByIdAndDeletedIsFalse(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.cell-doesnt-exist", null, LocaleContextHolder.getLocale())));

        if (!storeCell.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakId)) {
            throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.cell-doesnt-exist", null, LocaleContextHolder.getLocale()));
        }

        storeCell.setDeleted(true);
        storeCellRepository.save(storeCell);
    }

    @Override
    public void addProductToCell(Long cellId, String productArticle, String keycloakId) {
        final var storeCell = storeCellRepository.findByIdAndDeletedIsFalse(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.cell-doesnt-exist", null, LocaleContextHolder.getLocale())));

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
        final var storeCell = storeCellRepository.findByIdAndDeletedIsFalse(cellId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.cell-doesnt-exist", null, LocaleContextHolder.getLocale())));

        var kaspiStoreOfCell = storeCell.getKaspiStore();

        if (!kaspiStoreOfCell.getWonderUser().getKeycloakId().equals(keycloakId)) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-cell-service-impl.cell-doesnt-exist", null, LocaleContextHolder.getLocale()));
        }

        storeCellMapper.updateEntity(storeCell, storeCellChangeRequest);

        storeCellRepository.save(storeCell);
    }

    private StoreEmployee validateEmployee(String keycloakId, Long kaspiStoreId) {
        var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.employee-not-found", null, LocaleContextHolder.getLocale())));

        var employeeWorkStore = employee.getKaspiStore();

        log.info("Employee work store: {}, requested store: {}", employeeWorkStore.getId(), kaspiStoreId);


        if (!employeeWorkStore.getId().equals(kaspiStoreId)) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-cell-service-impl.you-do-not-have-permission-to-add-this-product-to-cell", null, LocaleContextHolder.getLocale()));
        }
        return employee;
    }

    private SupplyBoxProduct validateProduct(String article, Long kaspiStoreId) {
        final var supplyBoxProduct = supplyBoxProductsRepository.findByArticle(article)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.store-cell-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));

        validateProductForStoreCell(supplyBoxProduct, kaspiStoreId);
        return supplyBoxProduct;
    }

    private void validateProductForStoreCell(SupplyBoxProduct supplyBoxProduct, Long kaspiStoreId) {
        if (!supplyBoxProduct.getSupplyBox().getSupply().getKaspiStore().getId().equals(kaspiStoreId)) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-cell-service-impl.you-do-not-have-permission-to-add-this-product-to-cell", null, LocaleContextHolder.getLocale()));
        }
        if (supplyBoxProduct.getState() == ProductStateInStore.SOLD)
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-cell-service-impl.product-already-sold", null, LocaleContextHolder.getLocale()));
    }
}
