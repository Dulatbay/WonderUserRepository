package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.entities.Product;
import kz.wonder.wonderuserrepository.entities.ProductPrice;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.ProductPriceRepository;
import kz.wonder.wonderuserrepository.repositories.ProductRepository;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;

    @Override
    public void processExcelFile(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String vendorCode = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();
                String link = row.getCell(2).getStringCellValue();
                boolean enabled = row.getCell(3).getBooleanCellValue();
                Double priceAlmaty = row.getCell(4).getNumericCellValue();
                Double priceAstana = row.getCell(5).getNumericCellValue();

                Product product = productRepository.findByVendorCode(vendorCode)
                        .orElse(new Product(vendorCode, name, link, enabled));

                product.setName(name);
                product.setLink(link);
                product.setEnabled(enabled);

                product = productRepository.save(product);

                ProductPrice almatyPrice = productPriceRepository.findByProductAndKaspiCityName(product, "Almaty")
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));
                almatyPrice.setPrice(priceAlmaty);
                almatyPrice.setUpdatedAt(LocalDateTime.now());
                productPriceRepository.save(almatyPrice);

                ProductPrice astanaPrice = productPriceRepository.findByProductAndKaspiCityName(product, "Astana")
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));

                astanaPrice.setPrice(priceAstana);
                astanaPrice.setUpdatedAt(LocalDateTime.now());
                productPriceRepository.save(astanaPrice);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
