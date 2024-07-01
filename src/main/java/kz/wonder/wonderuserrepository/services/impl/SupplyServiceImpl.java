package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.filemanager.client.api.FileManagerApi;
import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.SupplyScanRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.entities.enums.SupplyState;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.BarcodeMapper;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;
    private final BarcodeMapper barcodeMapper;

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
                                messageSource.getMessage("services-impl.supply-service-impl.product-with-id", null, LocaleContextHolder.getLocale()) + " " + vendorCode + " " + messageSource.getMessage("services-impl.supply-service-impl.does-not-exist", null, LocaleContextHolder.getLocale())));

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
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.file-processing-failed", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SupplySellerResponse createSupply(SupplyCreateRequest createRequest, String userId) {
        final var store = kaspiStoreRepository.findById(createRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.store-not-found", null, LocaleContextHolder.getLocale())));

        if (!store.isEnabled())
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.store-not-enabled", null, LocaleContextHolder.getLocale()));


        final var user = userRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.user-not-found", null, LocaleContextHolder.getLocale())));


        log.info("Found store id: {}", store.getId());


        var availableTimes = store.getAvailableTimes();
        var selectedTime = createRequest.getSelectedTime();
        var dayOfWeekOfSelectedTime = selectedTime.getDayOfWeek();


        var isAvailableToSupply = false;


        for (var time : availableTimes) {
            if (time.getDayOfWeek().ordinal() == dayOfWeekOfSelectedTime.ordinal()) {
//                if (time.getCloseTime().isAfter(selectedTime.toLocalTime().minusMinutes(1)) && time.getOpenTime().isBefore(selectedTime.toLocalTime().plusMinutes(1))) {
                isAvailableToSupply = true;
                break;
//                }
            }
        }


        if (!isAvailableToSupply) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.store-not-operational-in-this-period", null, LocaleContextHolder.getLocale()));
        }


        Supply supply = supplyMapper.toSupplyEntity(createRequest, user, store);


        createRequest.getSelectedBoxes()
                .forEach(selectedBox -> {
                    final var boxType = boxTypeRepository.findByIdInStore(selectedBox.getSelectedBoxId(), store.getId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.supply-boxes-are-empty", null, LocaleContextHolder.getLocale())));

                    var supplyBox = new SupplyBox();
                    supplyBox.setBoxType(boxType);
                    supplyBox.setSupply(supply);


                    var selectedProducts = selectedBox.getProductQuantities();
                    selectedProducts.forEach(selectedProduct -> {
                        var product = productRepository.findByIdAndKeycloakIdAndDeletedIsFalse(selectedProduct.getProductId(), userId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.product-not-found", null, LocaleContextHolder.getLocale())));

                        for (int i = 0; i < selectedProduct.getQuantity(); i++) {
                            SupplyBoxProduct boxProducts = new SupplyBoxProduct();
                            boxProducts.setSupplyBox(supplyBox);
                            boxProducts.setProduct(product);
                            boxProducts.setState(ProductStateInStore.PENDING);

                            supplyBox.getSupplyBoxProducts().add(boxProducts);
                        }

                        if (supplyBox.getSupplyBoxProducts().isEmpty()) {
                            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.supply-boxes-are-empty", null, LocaleContextHolder.getLocale()));
                        }

                    });

                    supply.getSupplyBoxes().add(supplyBox);
                });

        if (supply.getSupplyBoxes().isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.supply-boxes-are-empty", null, LocaleContextHolder.getLocale()));
        }

        var createdSupply = supplyRepository.save(supply);

        log.info("Created supply id: {}", createdSupply.getId());
        log.info("Boxes count in created supply: {}", createdSupply.getSupplyBoxes().size());


        var generateBarCodes = CompletableFuture.runAsync(() -> {
                    log.info("Generating barcodes started, supply id: {}", createdSupply.getId());
                    final List<MultipartFile> multipartFilesBox = new ArrayList<>();
                    final List<MultipartFile> multipartFilesProducts = new ArrayList<>();

                    createdSupply.getSupplyBoxes()
                            .parallelStream()
                            .forEach(box -> {
                                var boxAdditionalText = List.of(
                                        messageSource.getMessage("services-impl.supply-service-impl.box", null, LocaleContextHolder.getLocale()) + ": " + box.getBoxType().getName(),
                                        messageSource.getMessage("services-impl.supply-service-impl.seller", null, LocaleContextHolder.getLocale()) + ": " + createdSupply.getAuthor().getKaspiToken().getSellerName());
                                multipartFilesBox.add(barcodeService.generateBarcode(box.getVendorCode(), boxAdditionalText));
                                box.getSupplyBoxProducts()
                                        .parallelStream()
                                        .forEach(supplyBoxProduct -> {
                                            var product = supplyBoxProduct.getProduct();
                                            var productAdditionalText = List.of(
                                                    product.getName().length() < 30 ? product.getName() : product.getName().substring(0, 30),
                                                    messageSource.getMessage("services-impl.supply-service-impl.seller", null, LocaleContextHolder.getLocale()) + ": " + createdSupply.getAuthor().getKaspiToken().getSellerName()
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

        return supplyMapper.toSupplySellerResponse(supply);
    }

    @Override
    public List<SupplyAdminResponse> getSuppliesOfAdmin(LocalDate startDate, LocalDate endDate, String userId, String fullName, String keycloakId) {
        var supplies = supplyRepository.findAllAdminSupplies(startDate.atStartOfDay(), endDate.atStartOfDay(), keycloakId);

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
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale()));

        log.info("Retrieving supply detail. Id: {}", id);

        return mapSupplyDetailsToResponse(supply);
    }

    @Override
    public List<SupplyProductResponse> getSuppliesDetailOfSeller(Long id, String keycloakId) {
        var supply = findSupplyById(id);

        String keycloakIdOfSupplyOwner = supply.getAuthor().getKeycloakId();

        if (!keycloakId.equals(keycloakIdOfSupplyOwner))
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale()));

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
                        messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale())
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
                        messageSource.getMessage("services-impl.supply-service-impl.store-employee-not-found", null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage("services-impl.supply-service-impl.create-store-employee", null, LocaleContextHolder.getLocale())));
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
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.supply-box-not-exist", null, LocaleContextHolder.getLocale())));
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
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.store-actions-only-available-to-store-employees", null, LocaleContextHolder.getLocale())));
        final var supply = supplyRepository.findById(supplyScanRequest.getSupplyId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale())));
        final var kaspiStore = employee.getKaspiStore();


        boolean employeeWorkHere = supply.getKaspiStore().getId().equals(kaspiStore.getId());

        if (!employeeWorkHere)
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale()));

        var now = LocalDateTime.now(ZONE_ID);

        supplyScanRequest.getProductCells()
                .forEach(productCell -> {
                    var cellCode = productCell.getCellCode();
                    var productArticles = productCell.getProductArticles();
                    var storeCell = storeCellRepository.findByKaspiStoreIdAndCode(kaspiStore.getId(), cellCode)
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.store-slot-does-not-exist", null, LocaleContextHolder.getLocale())));


                    Map<String, StoreCellProduct> storeCellProductsMap = new HashMap<>();

                    productArticles
                            .forEach(article -> {

                                if (storeCellProductsMap.containsKey(article))
                                    throw new IllegalArgumentException("Введите уникальные артикли");

                                var supplyBoxProduct = this.validateProduct(article, kaspiStore.getId());

                                supplyBoxProduct.setState(ProductStateInStore.ACCEPTED);
                                supplyBoxProduct.setAcceptedTime(now);
                                supplyBoxProductsRepository.save(supplyBoxProduct);

                                StoreCellProduct storeCellProduct = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                                        .orElse(new StoreCellProduct());

                                storeCellProduct.setStoreEmployee(employee);
                                storeCellProduct.setStoreCell(storeCell);
                                storeCellProduct.setSupplyBoxProduct(supplyBoxProduct);
                                storeCellProduct.setBusy(true);

                                storeCellProductsMap.put(article, storeCellProduct);

                            });

                    storeCellProductRepository.saveAll(storeCellProductsMap.values());
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
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale())));

        return getSellerSupplyReport(supply);
    }

    @Override
    public void uploadAuthorityDocument(MultipartFile file, Long supplyId, String keycloakId) {
        var supply = supplyRepository.findByIdAndAuthorKeycloakId(supplyId, keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale())));

        var authorityDocumentName = fileManagerApi
                .uploadFiles(FILE_MANAGER_SUPPLY_AUTHORITY_DOCUMENTS_DIR, List.of(file), false).getBody().getFirst();

        log.info("uploaded authority document: {}", file.getName());

        supply.setPathToAuthorityDocument(barcodeMapper.getPathToAuthorityDocument(authorityDocumentName));

        supplyRepository.save(supply);

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
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, messageSource.getMessage("services-impl.supply-service-impl.store-employee-not-found", null, LocaleContextHolder.getLocale()), messageSource.getMessage("services-impl.supply-service-impl.create-store-employee", null, LocaleContextHolder.getLocale())));
    }

    private Supply findSupplyById(Long supplyId) {
        return supplyRepository.findById(supplyId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, messageSource.getMessage("services-impl.supply-service-impl.offer-not-found", null, LocaleContextHolder.getLocale()), messageSource.getMessage("services-impl.supply-service-impl.try-using-different-parameters", null, LocaleContextHolder.getLocale())));
    }

    private void validateStoreEmployeeAndSupply(StoreEmployee storeEmployee, Supply supply) {
        if (!Objects.equals(supply.getKaspiStore().getId(), storeEmployee.getKaspiStore().getId())) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.supply-service-impl.supply-not-exist", null, LocaleContextHolder.getLocale()));
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

    private SupplyBoxProduct validateProduct(String article, Long kaspiStoreId) {
        final var supplyBoxProduct = supplyBoxProductsRepository.findByArticle(article)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Товар не существует"));

        validateProductForStoreCell(supplyBoxProduct, kaspiStoreId);
        return supplyBoxProduct;
    }

    private void validateProductForStoreCell(SupplyBoxProduct supplyBoxProduct, Long kaspiStoreId) {
        if (!supplyBoxProduct.getSupplyBox().getSupply().getKaspiStore().getId().equals(kaspiStoreId)) {
            throw new IllegalArgumentException("У вас нет разрешения на добавление этого товара в ячейку.");
        }
        if (supplyBoxProduct.getState() != ProductStateInStore.PENDING && supplyBoxProduct.getState() != ProductStateInStore.ACCEPTED)
            throw new IllegalArgumentException("Товар уже невозможно отсканировать");
    }

}
