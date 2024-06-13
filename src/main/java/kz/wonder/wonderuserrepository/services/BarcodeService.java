package kz.wonder.wonderuserrepository.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

public interface BarcodeService {
    MultipartFile generateProductBarcode(String productName, String productArticle, String boxVendorCode, String shopName);
}
