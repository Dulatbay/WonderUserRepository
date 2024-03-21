package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.SupplyProcessFileResponse;
import kz.wonder.wonderuserrepository.entities.Supply;
import kz.wonder.wonderuserrepository.entities.SupplyBox;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProducts;
import kz.wonder.wonderuserrepository.entities.SupplyStates;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.ProductRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.services.SupplyService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;

@Service
@RequiredArgsConstructor
public class SupplyServiceImpl implements SupplyService {

    private final ProductRepository productRepository;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final BoxTypeRepository boxTypeRepository;
    private final UserRepository userRepository;

    @Override
    public List<SupplyProcessFileResponse> processFile(MultipartFile file, String userId) {
        final var response = new ArrayList<SupplyProcessFileResponse>();

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

                response.add(
                        SupplyProcessFileResponse.builder()
                                .productId(product.getId())
                                .name(product.getName())
                                .vendorCode(product.getVendorCode())
                                .quantity(quantity)
                                .build()
                );

            }
            return response;
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("File process failed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createSupply(SupplyCreateRequest createRequest, String userId) {
        final var store = kaspiStoreRepository.findById(createRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

        final var user = userRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "User doesn't exist"));


        Supply supply = new Supply();
        supply.setAuthor(user);
        supply.setKaspiStore(store);
        supply.setSupplyStates(SupplyStates.START);


        createRequest.getSelectedBoxes()
                .forEach(selectedBox -> {
                    var boxType = boxTypeRepository.findById(selectedBox.getSelectedBoxId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Box doesn't exist"));




                });


    }


}
