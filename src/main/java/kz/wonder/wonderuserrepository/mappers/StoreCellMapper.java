package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.request.StoreCellChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;
import kz.wonder.wonderuserrepository.entities.StoreCell;
import kz.wonder.wonderuserrepository.entities.StoreCellProduct;
import kz.wonder.wonderuserrepository.entities.StoreEmployee;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import org.springframework.stereotype.Component;

@Component
public class StoreCellMapper {
    public StoreCellResponse toResponse(StoreCell storeCell) {
        StoreCellResponse storeCellResponse = new StoreCellResponse();
        storeCellResponse.setCell(storeCell.getCell());
        storeCellResponse.setCol(storeCell.getCol());
        storeCellResponse.setRow(storeCell.getRow());
        storeCellResponse.setComment(storeCell.getComment());
        storeCellResponse.setWidth(storeCell.getWidth());
        storeCellResponse.setHeight(storeCell.getHeight());
        storeCellResponse.setDepth(storeCell.getDepth());
        storeCellResponse.setId(storeCell.getId());
        return storeCellResponse;
    }

    public StoreCell toEntity(StoreCellCreateRequest storeCellCreateRequest) {
        StoreCell storeCell = new StoreCell();
        storeCell.setCell(storeCellCreateRequest.getCell());
        storeCell.setCol(storeCellCreateRequest.getCol());
        storeCell.setRow(storeCellCreateRequest.getRow());
        storeCell.setComment(storeCellCreateRequest.getComment());
        storeCell.setWidth(storeCellCreateRequest.getWidth());
        storeCell.setDepth(storeCellCreateRequest.getDepth());
        storeCell.setHeight(storeCellCreateRequest.getHeight());
        return storeCell;
    }

    public void updateEntity(StoreCell storeCell, StoreCellChangeRequest storeCellChangeRequest) {
        storeCell.setRow(storeCellChangeRequest.getRow());
        storeCell.setCol(storeCellChangeRequest.getCol());
        storeCell.setCell(storeCellChangeRequest.getCell());
        storeCell.setComment(storeCellChangeRequest.getComment());
        storeCell.setWidth(storeCellChangeRequest.getWidth());
        storeCell.setHeight(storeCellChangeRequest.getHeight());
        storeCell.setDepth(storeCellChangeRequest.getDepth());
    }

    public StoreCellProduct toStoreCellProduct(StoreCell storeCell, SupplyBoxProduct supplyBoxProduct, StoreEmployee storeEmployee) {
        StoreCellProduct storeCellProduct = new StoreCellProduct();
        storeCellProduct.setStoreCell(storeCell);
        storeCellProduct.setSupplyBoxProduct(supplyBoxProduct);
        storeCellProduct.setStoreEmployee(storeEmployee);
        return storeCellProduct;
    }
}
