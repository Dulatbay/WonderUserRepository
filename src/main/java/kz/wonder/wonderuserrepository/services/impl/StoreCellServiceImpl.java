package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;
import kz.wonder.wonderuserrepository.entities.StoreCell;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.StoreCellRepository;
import kz.wonder.wonderuserrepository.services.StoreCellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreCellServiceImpl implements StoreCellService {
    private final StoreCellRepository storeCellRepository;
    private final KaspiStoreRepository kaspiStoreRepository;

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

        log.info("Created store cell with id: {}", storeCell.getId());

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

        if(!storeCell.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakId)) {
            throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Store doesn't exist");
        }
        storeCellRepository.delete(storeCell);
    }
}
