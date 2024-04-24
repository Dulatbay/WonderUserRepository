package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.SupplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupplyServiceImpl implements SupplyService {


    private final ProductRepository productRepository;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final BoxTypeRepository boxTypeRepository;
    private final UserRepository userRepository;
    private final SupplyRepository supplyRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;

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
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(),
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
            log.info("IllegalStateException :", e);
            throw new IllegalArgumentException("File process failed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long createSupply(SupplyCreateRequest createRequest, String userId) {

        // todo: при создании поставки нужно проверить:
        //  1) время(работает ли в этот день склад)
        //  2) Есть ли там доступные места(хотя это врядли)
        //  3) Генерация номера ячейки

        final var store = kaspiStoreRepository.findById(createRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

        final var user = userRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "WonderUser doesn't exist"));

        log.info("Found store id: {}", store.getId());


        var availableTimes = store.getAvailableTimes();
        var selectedTime = createRequest.getSelectedTime();
        var dayOfWeekOfSelectedTime = selectedTime.getDayOfWeek();


        var isAvailableToSupply = false;



        for(var time : availableTimes){
            if(time.getDayOfWeek().ordinal() == dayOfWeekOfSelectedTime.ordinal()){
                if(time.getCloseTime().isBefore(selectedTime.toLocalTime()) && time.getOpenTime().isAfter(selectedTime.toLocalTime())){
                    isAvailableToSupply = true;
                    break;
                }
            }
        }



        if(!isAvailableToSupply) {
            throw new IllegalArgumentException("Store don't work in this period");
        }


        Supply supply = new Supply();
        supply.setAuthor(user);
        supply.setKaspiStore(store);
        supply.setSupplyState(SupplyState.START);
        supply.setSupplyBoxes(new ArrayList<>());
        supply.setSelectedTime(createRequest.getSelectedTime());


        createRequest.getSelectedBoxes()
                .forEach(selectedBox -> {
                    final var boxType = boxTypeRepository.findByIdInStore(selectedBox.getSelectedBoxId(), store.getId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Box doesn't exist"));

                    var supplyBox = new SupplyBox();
                    supplyBox.setBoxType(boxType);
                    supplyBox.setSupplyBoxProducts(new ArrayList<>());
                    supplyBox.setSupply(supply);


                    var selectedProducts = selectedBox.getProductQuantities();
                    selectedProducts.forEach(selectedProduct -> {
                        var product = productRepository.findByIdAndKeycloakId(selectedProduct.getProductId(), userId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));


                        for (int i = 0; i < selectedProduct.getQuantity(); i++) {
                            SupplyBoxProduct boxProducts = new SupplyBoxProduct();
                            boxProducts.setSupplyBox(supplyBox);
                            boxProducts.setProduct(product);
                            boxProducts.setState(ProductStateInStore.PENDING);
                            supplyBox.getSupplyBoxProducts().add(boxProducts);
                        }

                        supply.getSupplyBoxes().add(supplyBox);
                    });
                });

        var created = supplyRepository.save(supply);

        log.info("Created supply id: {}", created.getId());
        log.info("Products size in create supply: {}", created.getSupplyBoxes().size());

        return created.getId();
    }

    @Override
    public List<SupplyAdminResponse> getSuppliesOfAdmin(LocalDate startDate, LocalDate endDate, String userId, String fullName) {
        var supplies = supplyRepository.findAllByCreatedAtBetween(startDate.atStartOfDay(), endDate.atStartOfDay());

        log.info("Supplies size: {}", supplies.size());

        return supplies.stream().map(supply -> {
            SupplyAdminResponse supplyAdminResponse = new SupplyAdminResponse();
            supplyAdminResponse.setId(supply.getId());
            supplyAdminResponse.setSupplyState(supply.getSupplyState());
            supplyAdminResponse.setSupplyAcceptTime(supply.getAcceptedTime());
            supplyAdminResponse.setSupplyCreatedTime(supply.getCreatedAt());
            supplyAdminResponse.setSeller(new SupplyAdminResponse.Seller(userId, fullName));
            return supplyAdminResponse;
        }).toList();
    }

    @Override
    public List<SupplyProductResponse> getSuppliesDetail(Long id) {
        var supply = findSupplyById(id);
        return mapSupplyDetailsToResponse(supply);
    }

    @Override
    public List<SupplyProductResponse> getSuppliesDetail(Long id, String keycloakId) {
        var supply = findSupplyById(id);

        String keycloakIdOfStoreOwner = supply.getKaspiStore().getWonderUser().getKeycloakId();
        if (!isIdentityMatched(keycloakId, keycloakIdOfStoreOwner))
            throw new IllegalArgumentException("Supply doesn't exist");

        return mapSupplyDetailsToResponse(supply);
    }

    @Override
    public List<SupplyProductResponse> getSuppliesDetailOfSeller(Long id, String keycloakId) {
        var supply = findSupplyById(id);

        String keycloakIdOfSupplyOwner = supply.getAuthor().getKeycloakId();
        if (!isIdentityMatched(keycloakId, keycloakIdOfSupplyOwner))
            throw new IllegalArgumentException("Supply doesn't exist");

        return mapSupplyDetailsToResponse(supply);
    }

    private List<SupplyProductResponse> mapSupplyDetailsToResponse(Supply supply) {
        var supplyProductsRes = new ArrayList<SupplyProductResponse>();

        var shopName = supply.getAuthor().getKaspiToken().getSellerName();

        supply.getSupplyBoxes()
                .forEach(supplyBox ->
                        supplyBox
                                .getSupplyBoxProducts()
                                .forEach(supplyBoxProducts -> {
                                    var product = supplyBoxProducts.getProduct();
                                    SupplyProductResponse supplyProductResponse = new SupplyProductResponse();
                                    supplyProductResponse.setName(product.getName());
                                    supplyProductResponse.setArticle(supplyBoxProducts.getArticle());
                                    supplyProductResponse.setVendorCode(product.getVendorCode());
                                    supplyProductResponse.setBoxBarCode(supplyBox.getVendorCode());
                                    supplyProductResponse.setStoreAddress(supply.getKaspiStore().getFormattedAddress());
                                    supplyProductResponse.setBoxTypeName(supplyBox.getBoxType().getName());
                                    supplyProductResponse.setShopName(shopName);
                                    supplyProductsRes.add(supplyProductResponse);
                                })
                );
        return supplyProductsRes;
    }

    private boolean isIdentityMatched(String requestedIdentity, String entityIdentity) {
        return requestedIdentity.equals(entityIdentity);
    }

    @Override
    public List<SupplySellerResponse> getSuppliesOfSeller(String keycloakId, LocalDate startDate, LocalDate endDate) {
        var supplies = supplyRepository.findAllByCreatedAtBetweenAndAuthorKeycloakId(
                startDate.atStartOfDay(),
                endDate.atStartOfDay(),
                keycloakId);
        return supplies
                .stream()
                .map(supply ->
                        SupplySellerResponse.builder()
                                .supplyCreatedTime(supply.getCreatedAt())
                                .supplyAcceptTime(supply.getAcceptedTime())
                                .supplyState(supply.getSupplyState())
                                .id(supply.getId())
                                .build()
                ).collect(Collectors.toList());
    }

    @Override
    public List<SupplyReportResponse> getSupplyReport(Long supplyId, String keycloakId) {
        var supply = supplyRepository.findByIdAndAuthorKeycloakId(supplyId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Supply doesn't exist"
                ));

        Map<Long, SupplyReportResponse> supplyReportResponseMap = new HashMap<>();
        processSupplyBoxes(supply, supplyReportResponseMap);

        return new ArrayList<>(supplyReportResponseMap.values());
    }

    @Override
    public List<SupplyStorageResponse> getSuppliesOfStorage(Long employeeId, LocalDate startDate, LocalDate endDate) {

        long startUnixTimestamp = startDate.atStartOfDay().atZone(ZONE_ID).toInstant().getEpochSecond();
        long endUnixTimestamp = endDate.atStartOfDay().plusDays(1).atZone(ZONE_ID).toInstant().getEpochSecond();


        var supplies = supplyRepository.findAllSuppliesOfStorage(
                employeeId, startUnixTimestamp, endUnixTimestamp);

        var suppliesByDayOfWeek = getSuppliesByDay(supplies);

        return suppliesByDayOfWeek.entrySet()
                .stream().map(
                        entry -> SupplyStorageResponse.builder()
                                .date(entry.getKey())
                                .supplies(entry.getValue()
                                        .stream()
                                        .map(this::buildSupplyOfStorageResponse)
                                        .collect(Collectors.toList()))
                                .build()
                ).collect(Collectors.toList());
    }

    @Override
    public List<SupplyStorageResponse> getSuppliesOfStorage(String keycloakId, LocalDate startDate, LocalDate endDate) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                        "Store employee doesn't exist", "Create store employee"));
        return this.getSuppliesOfStorage(storeEmployee.getId(), startDate, endDate);
    }

    @Override
    public ProductStorageResponse getSuppliesProducts(String keycloakId, Long supplyId) {
        final var storeEmployee = findStoreEmployeeByKeycloakId(keycloakId);
        final var supply = findSupplyById(supplyId);
        validateStoreEmployeeAndSupply(storeEmployee, supply);

        return ProductStorageResponse.builder()
                .storeId(supply.getKaspiStore().getId())
                .supplyId(supplyId)
                .products(buildProducts(supply))
                .storeAddress(supply.getKaspiStore().getFormattedAddress())
                .build();
    }

    private StoreEmployee findStoreEmployeeByKeycloakId(String keycloakId) {
        return storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Store employee doesn't exist", "Create store employee"));
    }

    private Supply findSupplyById(Long supplyId) {
        return supplyRepository.findById(supplyId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Supply doesn't exist", "Try with another params"));
    }

    private void validateStoreEmployeeAndSupply(StoreEmployee storeEmployee, Supply supply) {
        if (!Objects.equals(supply.getKaspiStore().getId(), storeEmployee.getKaspiStore().getId())) {
            throw new IllegalArgumentException("Supply doesn't exist");
        }
    }

    private ArrayList<ProductStorageResponse.Product> buildProducts(Supply supply) {
        ArrayList<ProductStorageResponse.Product> products = new ArrayList<>();

        supply.getSupplyBoxes().forEach(supplyBox ->
                supplyBox.getSupplyBoxProducts().forEach(supplyBoxProducts -> {
                    var product = ProductStorageResponse.Product.builder()
                            .article(supplyBoxProducts.getArticle())
                            .productStateInStore(supplyBoxProducts.getState())
                            .typeOfBoxName(supplyBox.getBoxType().getName())
                            .vendorCodeOfBox(supplyBox.getVendorCode())
                            .vendorCode(supplyBoxProducts.getProduct().getVendorCode())
                            .name(supplyBoxProducts.getProduct().getName())
                            .build();
                    products.add(product);
                })
        );

        return products;
    }

    private SupplyStorageResponse.Supply buildSupplyOfStorageResponse(Supply supply) {
        var author = supply.getAuthor();
        return SupplyStorageResponse.Supply.builder()
                .supplyId(supply.getId())
                .sellerId(author.getId())
                .sellerName(author.getKaspiToken() != null ? author.getKaspiToken().getSellerName() : "N/A")
                .supplyState(supply.getSupplyState())
                .build();
    }

    private Map<LocalDate, List<Supply>> getSuppliesByDay(List<Supply> supplies) {
        Map<LocalDate, List<Supply>> suppliesPerDayOfWeek = new HashMap<>();


        for (Supply supply : supplies) {
            var createdDate = supply.getSelectedTime().toLocalDate();
            var suppliesInDayOfWeek = suppliesPerDayOfWeek.getOrDefault(createdDate, new ArrayList<>());
            suppliesInDayOfWeek.add(supply);
            suppliesPerDayOfWeek.put(createdDate, suppliesInDayOfWeek);
        }
        return suppliesPerDayOfWeek;
    }

    private void processSupplyBoxes(Supply supply, Map<Long, SupplyReportResponse> reportMap) {
        supply.getSupplyBoxes().forEach(supplyBox -> {
            var supplyBoxProducts = supplyBox.getSupplyBoxProducts();
            supplyBoxProducts.forEach(product -> processSupplyBoxProduct(product, reportMap));
        });
    }

    private void processSupplyBoxProduct(SupplyBoxProduct supplyBoxProduct, Map<Long, SupplyReportResponse> reportMap) {
        var product = supplyBoxProduct.getProduct();
        var productId = product.getId();

        var report = reportMap.computeIfAbsent(productId, id -> SupplyReportResponse.builder()
                .productName(product.getName())
                .productBarcode(product.getVendorCode())
                .countOfProductDeclined(0L)
                .countOfProductAccepted(0L)
                .countOfProductPending(0L)
                .build());

        updateReportCounts(supplyBoxProduct, report);
    }

    private void updateReportCounts(SupplyBoxProduct supplyBoxProduct, SupplyReportResponse report) {
        switch (supplyBoxProduct.getState()) {
            case ACCEPTED:
                report.setCountOfProductAccepted(report.getCountOfProductAccepted() + 1);
                break;
            case DECLINED:
                report.setCountOfProductDeclined(report.getCountOfProductDeclined() + 1);
                break;
            case PENDING:
                report.setCountOfProductPending(report.getCountOfProductPending() + 1);
                break;
            default:
        }
    }

}
