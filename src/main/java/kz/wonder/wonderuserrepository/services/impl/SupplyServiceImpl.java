package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.entities.ProductQuantity;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.ProductRepository;
import kz.wonder.wonderuserrepository.services.SupplyService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Iterator;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;

@Service
@RequiredArgsConstructor
public class SupplyServiceImpl implements SupplyService {

    private final ProductRepository productRepository;
    private final KaspiStoreRepository kaspiStoreRepository;

    @Override
    public void createSupply(MultipartFile file, String userId, Long storeId) {

        final var store = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

        try (final Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String vendorCode = getStringFromExcelCell(row.getCell(0));
                long quantity = (long) row.getCell(1).getNumericCellValue();

                var product = productRepository.findByVendorCodeAndKeycloakId(vendorCode, userId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                String.format("Product by id %s doesn't exist: ", vendorCode)));

                var productQuantity = new ProductQuantity();

                productQuantity.setProduct(product);
                productQuantity.setKaspiStore(store);
                productQuantity.setQuantity(quantity);

                // todo: чекнуть чтобы не было одинаковых товаров одновременно со складами в productQuantity

                product.getProductQuantities().add(productQuantity);


                productRepository.save(product);
            }
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("File process failed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
