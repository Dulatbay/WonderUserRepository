package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.SellerSupplyReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BarcodeService {
    MultipartFile generateBarcode(String barcodeText, List<String> additionalTexts);

    MultipartFile generateSupplyReport(SellerSupplyReport supply);
}
