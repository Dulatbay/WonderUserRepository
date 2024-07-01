package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.entities.Supply;
import kz.wonder.wonderuserrepository.entities.SupplyBox;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.*;

@Component
public class BarcodeMapper {
    @Value("${application.file-api.url}")
    private String fileApiUrl;

    public String getPathToReport(Supply supply) {
        return fileApiUrl + "/" + FILE_MANAGER_SUPPLY_REPORT_DIR + "/retrieve/files/supply_report_" + supply.getId() + ".pdf";
    }

    public String getPathToAuthorityDocument(String documentName) {
        return fileApiUrl + "/" + FILE_MANAGER_SUPPLY_AUTHORITY_DOCUMENTS_DIR + "/" + documentName;
    }

    public String getPathToProductBarcode(SupplyBoxProduct supplyBoxProduct) {
        return fileApiUrl + "/" + FILE_MANAGER_PRODUCT_BARCODE_DIR + "/retrieve/files/" + supplyBoxProduct.getPathToBarcode();
    }

    public String getPathToBoxBarcode(SupplyBox supplyBox) {
        return fileApiUrl + "/" + FILE_MANAGER_BOX_BARCODE_DIR + "/retrieve/files/" + supplyBox.getPathToBarcode();
    }
}
