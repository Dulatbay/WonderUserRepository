package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.filemanager.client.api.FileManagerApi;
import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.SupplyScanRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.SupplyMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.BarcodeService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.*;

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
    private final SupplyBoxRepository supplyBoxRepository;
    private final StoreCellRepository storeCellRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    private final SupplyMapper supplyMapper;
    private final BarcodeService barcodeService;
    private final FileManagerApi fileManagerApi;

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

                var product = productRepository.findByVendorCodeAndKeycloakIdAndDeletedIsFalse(vendorCode, userId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                String.format("Товар с id %s не существует: ", vendorCode)));

                response.add(
                        SupplyProcessFileResponse.builder()
                                .productId(product.getId())
                                .name(product.getName())
                                .vendorCode(product.getVendorCode())
                                .quantity(quantity)
                                .build()
                );

            }

            log.info("Response size: {}", response.size());

            return response;
        } catch (IllegalStateException e) {
            log.error("IllegalStateException :", e);
            throw new IllegalArgumentException("Обработка файла не удалась");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long createSupply(SupplyCreateRequest createRequest, String userId) {
        final var store = kaspiStoreRepository.findById(createRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Магазин не существует"));

        if (!store.isEnabled())
            throw new IllegalArgumentException("Store is not enabled");

        final var user = userRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "WonderUser не существует"));


        log.info("Found store id: {}", store.getId());


        var availableTimes = store.getAvailableTimes();
        var selectedTime = createRequest.getSelectedTime();
        var dayOfWeekOfSelectedTime = selectedTime.getDayOfWeek();


        var isAvailableToSupply = false;


        for (var time : availableTimes) {
            if (time.getDayOfWeek().ordinal() == dayOfWeekOfSelectedTime.ordinal()) {
                if (time.getCloseTime().isAfter(selectedTime.toLocalTime().minusMinutes(1)) && time.getOpenTime().isBefore(selectedTime.toLocalTime().plusMinutes(1))) {
                    isAvailableToSupply = true;
                    break;
                }
            }
        }


        if (!isAvailableToSupply) {
            throw new IllegalArgumentException("Магазин не работает в этот период");
        }


        Supply supply = supplyMapper.toSupplyEntity(createRequest, user, store);


        createRequest.getSelectedBoxes()
                .forEach(selectedBox -> {
                    final var boxType = boxTypeRepository.findByIdInStore(selectedBox.getSelectedBoxId(), store.getId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Коробка не существует"));

                    var supplyBox = new SupplyBox();
                    supplyBox.setBoxType(boxType);
                    supplyBox.setSupplyBoxProducts(new ArrayList<>());
                    supplyBox.setSupply(supply);


                    var selectedProducts = selectedBox.getProductQuantities();
                    selectedProducts.forEach(selectedProduct -> {
                        var product = productRepository.findByIdAndKeycloakIdAndDeletedIsFalse(selectedProduct.getProductId(), userId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

                        for (int i = 0; i < selectedProduct.getQuantity(); i++) {
                            SupplyBoxProduct boxProducts = new SupplyBoxProduct();
                            boxProducts.setSupplyBox(supplyBox);
                            boxProducts.setProduct(product);
                            boxProducts.setState(ProductStateInStore.PENDING);

                            supplyBox.getSupplyBoxProducts().add(boxProducts);
                        }

                        if (supplyBox.getSupplyBoxProducts().isEmpty()) {
                            throw new IllegalArgumentException("Коробки с припасами пусты");
                        }

                        supply.getSupplyBoxes().add(supplyBox);
                    });
                });

        if (supply.getSupplyBoxes().isEmpty()) {
            throw new IllegalArgumentException("Коробки с припасами пусты");
        }

        var createdSupply = supplyRepository.save(supply);

        log.info("Created supply id: {}", createdSupply.getId());
        log.info("Products size in create supply: {}", createdSupply.getSupplyBoxes().size());


        var generateBarCodes = CompletableFuture.runAsync(() -> {
                    log.info("Generating barcodes started, supply id: {}", createdSupply.getId());
                    final List<MultipartFile> multipartFilesBox = new ArrayList<>();
                    final List<MultipartFile> multipartFilesProducts = new ArrayList<>();

                    createdSupply.getSupplyBoxes()
                            .parallelStream()
                            .forEach(box -> {
                                var boxAdditionalText = List.of(
                                        "Коробка: " + box.getBoxType().getName(),
                                        "Продавец:" + createdSupply.getAuthor().getKaspiToken().getSellerName());
                                multipartFilesBox.add(barcodeService.generateBarcode(box.getVendorCode(), boxAdditionalText));
                                box.getSupplyBoxProducts()
                                        .parallelStream()
                                        .forEach(supplyBoxProduct -> {
                                            var product = supplyBoxProduct.getProduct();
                                            var productAdditionalText = List.of(
                                                    product.getName().substring(0, 30),
                                                    "Продавец:" + createdSupply.getAuthor().getKaspiToken().getSellerName()
                                            );
                                            multipartFilesProducts.add(barcodeService.generateBarcode(supplyBoxProduct.getArticle(), productAdditionalText));
                                        });
                            });
                    log.info("Barcodes to generating, boxes: {}, products:{}", multipartFilesBox.size(), multipartFilesProducts.size());

                    int batch = 50;
                    for (int i = 0; i < multipartFilesBox.size(); i += batch) {
                        var sublist = multipartFilesBox.subList(i, Math.min(multipartFilesBox.size(), i + batch));
                        fileManagerApi.uploadFiles(FILE_MANAGER_BOX_BARCODE_DIR, sublist, false);
                    }

                    for (int i = 0; i < multipartFilesProducts.size(); i += batch) {
                        var sublist = multipartFilesProducts.subList(i, Math.min(multipartFilesProducts.size(), i + batch));
                        fileManagerApi.uploadFiles(FILE_MANAGER_PRODUCT_BARCODE_DIR, sublist, false);
                    }

                    log.info("All barcodes generated uploaded");
                }
        );

        var generateSupply = CompletableFuture.runAsync(() -> {
            var generatedSupplyReport = barcodeService.generateSupplyReport(this.getSellerSupplyReport(createdSupply));
            fileManagerApi.uploadFiles(FILE_MANAGER_SUPPLY_REPORT_DIR, List.of(generatedSupplyReport), false);
        });

        CompletableFuture.allOf(generateSupply, generateBarCodes)
                .join();

        return createdSupply.getId();
    }

    @Override
    public List<SupplyAdminResponse> getSuppliesOfAdmin(LocalDate startDate, LocalDate endDate, String userId, String fullName, String keycloakId) {
        var supplies = supplyRepository.findAllByCreatedAtBetweenAndKaspiStore_WonderUserKeycloakId(startDate.atStartOfDay(), endDate.atStartOfDay(), keycloakId);

        log.info("Supplies size: {}", supplies.size());

        return supplies
                .stream()
                .map(supply -> supplyMapper.toSupplyAdminResponse(supply, userId, fullName))
                .toList();
    }

    @Override
    public List<SupplyProductResponse> getSuppliesDetail(Long id) {
        var supply = findSupplyById(id);
        log.info("Retrieving supply detail. Id: {}", id);
        return mapSupplyDetailsToResponse(supply);
    }

    @Override
    public List<SupplyProductResponse> getSuppliesDetail(Long id, String keycloakId) {
        var supply = findSupplyById(id);

        String keycloakIdOfStoreOwner = supply.getKaspiStore().getWonderUser().getKeycloakId();
        if (!keycloakId.equals(keycloakIdOfStoreOwner))
            throw new IllegalArgumentException("Поставки не существует");

        log.info("Retrieving supply detail. Id: {}", id);

        return mapSupplyDetailsToResponse(supply);
    }

    @Override
    public List<SupplyProductResponse> getSuppliesDetailOfSeller(Long id, String keycloakId) {
        var supply = findSupplyById(id);

        String keycloakIdOfSupplyOwner = supply.getAuthor().getKeycloakId();

        if (!keycloakId.equals(keycloakIdOfSupplyOwner))
            throw new IllegalArgumentException("Поставки не существует");

        log.info("Retrieving supply detail. Id: {}", id);

        return mapSupplyDetailsToResponse(supply);
    }

    private List<SupplyProductResponse> mapSupplyDetailsToResponse(Supply supply) {
        var supplyProductsRes = new ArrayList<SupplyProductResponse>();

        supply.getSupplyBoxes().forEach(
                supplyBox -> supplyBox.getSupplyBoxProducts().forEach(
                        supplyBoxProduct -> supplyProductsRes.add(supplyMapper.toSupplyProductResponse(supply, supplyBox, supplyBoxProduct))
                )
        );

        return supplyProductsRes;
    }

    @Override
    public List<SupplySellerResponse> getSuppliesOfSeller(String keycloakId, LocalDate startDate, LocalDate endDate) {
        var supplies = supplyRepository.findAllSellerSupplies(
                startDate.atStartOfDay(),
                endDate.atStartOfDay(),
                keycloakId);

        log.info("Supplies of seller with size: {}", supplies.size());

        return supplies
                .stream()
                .map(supplyMapper::toSupplySellerResponse).collect(Collectors.toList());
    }


    @Override
    public List<SupplyStateResponse> getSupplySellerState(Long supplyId, String keycloakId) {
        var supply = supplyRepository.findByIdAndAuthorKeycloakId(supplyId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Поставки не существует"
                ));

        Map<Long, SupplyStateResponse> supplyReportResponseMap = new HashMap<>();
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
                        "Сотрудник магазина не существует", "Создайте сотрудника магазина"));
        return this.getSuppliesOfStorage(storeEmployee.getId(), startDate, endDate);
    }

    @Override
    public ProductStorageResponse getSuppliesProducts(String keycloakId, Long supplyId) {
        final var storeEmployee = findStoreEmployeeByKeycloakId(keycloakId);
        final var supply = findSupplyById(supplyId);
        validateStoreEmployeeAndSupply(storeEmployee, supply);

        return supplyMapper.toProductStorageResponse(supply);
    }

    @Override
    public ProductStorageResponse getSuppliesProducts(String keycloakId, String boxVendorCode, boolean isSuperAdmin) {
        final var supplyBox = supplyBoxRepository.findByVendorCode(boxVendorCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Коробка поставки не существует"));
        final var supply = supplyBox.getSupply();

        if (!isSuperAdmin) {
            final StoreEmployee storeEmployee = findStoreEmployeeByKeycloakId(keycloakId);
            validateStoreEmployeeAndSupply(storeEmployee, supply);
        }

        return supplyMapper.toProductStorageResponse(supply);
    }


    @Transactional
    @Override
    public void processSupplyByEmployee(String keycloakId, SupplyScanRequest supplyScanRequest) {
        final var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), "Действия над магазином доступны только сотрудникам склада"));
        final var supply = supplyRepository.findById(supplyScanRequest.getSupplyId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Поставка не существует"));
        final var kaspiStore = employee.getKaspiStore();


        boolean employeeWorkHere = supply.getKaspiStore().getId().equals(kaspiStore.getId());

        if (!employeeWorkHere) throw new IllegalArgumentException("Supply doesn't exist");

        var now = LocalDateTime.now(ZONE_ID);

        supplyScanRequest.getProductCells()
                .forEach(productCell -> {
                    var cellCode = productCell.getCellCode();
                    var productArticles = productCell.getProductArticles();
                    var storeCell = storeCellRepository.findByKaspiStoreIdAndCode(kaspiStore.getId(), cellCode)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Ячейка магазина не существует"));

                    List<StoreCellProduct> storeCellProducts = productArticles
                            .stream()
                            .map(article -> {
                                var supplyBoxProduct = supplyBoxProductsRepository.findByArticle(article)
                                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

                                supplyBoxProduct.setState(ProductStateInStore.ACCEPTED);
                                supplyBoxProduct.setAcceptedTime(now);
                                supplyBoxProductsRepository.save(supplyBoxProduct);

                                StoreCellProduct storeCellProduct = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                                        .orElse(new StoreCellProduct());

                                storeCellProduct.setStoreEmployee(employee);
                                storeCellProduct.setStoreCell(storeCell);
                                storeCellProduct.setSupplyBoxProduct(supplyBoxProduct);
                                storeCellProduct.setBusy(true);

                                return storeCellProduct;
                            }).toList();

                    storeCellProductRepository.saveAll(storeCellProducts);
                });

        supply.setAcceptedTime(now);
        var isAccepted = supply.getSupplyBoxes()
                .stream()
                .noneMatch(supplyBox -> supplyBox.getSupplyBoxProducts()
                        .stream()
                        .anyMatch(supplyBoxProduct -> supplyBoxProduct.getState() == ProductStateInStore.PENDING));
        supply.setSupplyState(isAccepted ? SupplyState.ACCEPTED : SupplyState.IN_PROGRESS);
    }

    @Override
    public SellerSupplyReport getSupplySellerReport(Long supplyId, String keycloakId) {
        var supply = supplyRepository.findByIdAndAuthorKeycloakId(supplyId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Supply doesn't exist"));

        return getSellerSupplyReport(supply);
    }

    public SellerSupplyReport getSellerSupplyReport(Supply supply) {
        var store = supply.getKaspiStore();

        SellerSupplyReport sellerSupplyReport = new SellerSupplyReport();
        sellerSupplyReport.setSupplyId(supply.getId());
        sellerSupplyReport.setSupplyCreationDate(supply.getCreatedAt());
        sellerSupplyReport.setSupplySelectedDate(supply.getSelectedTime());
        sellerSupplyReport.setSupplyDeliveredDate(sellerSupplyReport.getSupplyDeliveredDate());
        sellerSupplyReport.setSupplyAcceptanceDate(supply.getAcceptedTime());
        sellerSupplyReport.setFormattedAddress(store.getFormattedAddress());

        var supplyBoxes = supply.getSupplyBoxes();


        List<SellerSupplyReport.SupplyBoxInfo> supplyBoxInfos = supplyBoxes.stream().map(supplyBox -> {
            var boxType = supplyBox.getBoxType();
            var supplyBoxProducts = supplyBox.getSupplyBoxProducts();


            Map<Long, SellerSupplyReport.SupplyProductInfo> supplyProductsMap = new HashMap<>();

            SellerSupplyReport.SupplyBoxInfo supplyBoxInfo = new SellerSupplyReport.SupplyBoxInfo();
            supplyBoxInfo.setBoxName(boxType.getName());
            supplyBoxInfo.setSize(supplyBoxProducts.size());
            supplyBoxInfo.setBoxDescription(boxType.getDescription());
            supplyBoxInfo.setBoxVendorCode(supplyBox.getVendorCode());

            supplyBoxProducts.forEach(supplyBoxProduct -> {
                var product = supplyBoxProduct.getProduct();

                var supplyProductInfo = supplyProductsMap.get(product.getId());

                if (supplyProductInfo == null) {
                    supplyProductInfo = new SellerSupplyReport.SupplyProductInfo();
                    supplyProductInfo.setProductName(product.getName());
                    supplyProductInfo.setProductCount(1L);
                } else {
                    supplyProductInfo.setProductCount(supplyProductInfo.getProductCount() + 1L);
                }

                supplyProductsMap.put(product.getId(), supplyProductInfo);
            });

            supplyBoxInfo.setProductInfo(new ArrayList<>(supplyProductsMap.values()));

            return supplyBoxInfo;
        }).toList();

        sellerSupplyReport.setSupplyBoxInfo(supplyBoxInfos);
        return sellerSupplyReport;
    }

    private StoreEmployee findStoreEmployeeByKeycloakId(String keycloakId) {
        return storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Сотрудник магазина не существует", "Создайте сотрудника магазина"));
    }

    private Supply findSupplyById(Long supplyId) {
        return supplyRepository.findById(supplyId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Предложения не существует", "Попробуйте использовать другие параметры"));
    }

    private void validateStoreEmployeeAndSupply(StoreEmployee storeEmployee, Supply supply) {
        if (!Objects.equals(supply.getKaspiStore().getId(), storeEmployee.getKaspiStore().getId())) {
            throw new IllegalArgumentException("Поставки не существует");
        }
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

    private void processSupplyBoxes(Supply supply, Map<Long, SupplyStateResponse> reportMap) {
        supply.getSupplyBoxes().forEach(supplyBox -> {
            var supplyBoxProducts = supplyBox.getSupplyBoxProducts();
            supplyBoxProducts.forEach(product -> processSupplyBoxProduct(product, reportMap));
        });
    }

    private void processSupplyBoxProduct(SupplyBoxProduct supplyBoxProduct, Map<Long, SupplyStateResponse> reportMap) {
        var product = supplyBoxProduct.getProduct();
        var productId = product.getId();

        var report = reportMap.computeIfAbsent(productId, id -> SupplyStateResponse.builder()
                .productName(product.getName())
                .productBarcode(product.getVendorCode())
                .countOfProductDeclined(0L)
                .countOfProductAccepted(0L)
                .countOfProductPending(0L)
                .build());

        updateReportCounts(supplyBoxProduct, report);
    }

    private void updateReportCounts(SupplyBoxProduct supplyBoxProduct, SupplyStateResponse report) {
        switch (supplyBoxProduct.getState()) {
            case ACCEPTED:
            case SOLD:
            case ASSEMBLED:
            case WAITING_FOR_ASSEMBLY:
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
