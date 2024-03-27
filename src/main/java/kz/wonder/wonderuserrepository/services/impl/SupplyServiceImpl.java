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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupplyServiceImpl implements SupplyService {

	private final ProductRepository productRepository;
	private final KaspiStoreRepository kaspiStoreRepository;
	private final BoxTypeRepository boxTypeRepository;
	private final UserRepository userRepository;
	private final SupplyRepository supplyRepository;

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
	public void createSupply(SupplyCreateRequest createRequest, String userId) {
		final var store = kaspiStoreRepository.findById(createRequest.getStoreId())
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

		final var user = userRepository.findByKeycloakId(userId)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "WonderUser doesn't exist"));


		Supply supply = new Supply();
		supply.setAuthor(user);
		supply.setKaspiStore(store);
		supply.setSupplyState(SupplyState.START);
		supply.setSupplyBoxes(new ArrayList<>());

		createRequest.getSelectedBoxes()
				.forEach(selectedBox -> {
					final var boxType = boxTypeRepository.findById(selectedBox.getSelectedBoxId())
							.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Box doesn't exist"));

					var supplyBox = new SupplyBox();
					supplyBox.setBoxType(boxType);
					supplyBox.setSupplyBoxProducts(new ArrayList<>());
					supplyBox.setSupply(supply);

					selectedBox.getProductIds()
							.forEach(productId -> {
								var product = productRepository.findById(productId)
										.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Product doesn't exist"));
								SupplyBoxProducts boxProducts = new SupplyBoxProducts();
								boxProducts.setSupplyBox(supplyBox);
								boxProducts.setProduct(product);
								boxProducts.setState(ProductStateInStore.PENDING);
								supplyBox.getSupplyBoxProducts().add(boxProducts);
							});

					supply.getSupplyBoxes().add(supplyBox);
				});

		supplyRepository.save(supply);
	}

	@Override
	public List<SupplyAdminResponse> getSuppliesOfAdmin(LocalDate startDate, LocalDate endDate, String userId, String fullName) {
		var supplies = supplyRepository.findAllByCreatedAtBetween(startDate.atStartOfDay(), endDate.atStartOfDay());
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
		var supply = supplyRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Supply doesn't exist"));

		var supplyProductsRes = new ArrayList<SupplyProductResponse>();


		supply.getSupplyBoxes()
				.forEach(supplyBox ->
						supplyBox
								.getSupplyBoxProducts()
								.forEach(supplyBoxProducts -> {
									var product = supplyBoxProducts.getProduct();
									SupplyProductResponse supplyProductResponse = new SupplyProductResponse();
									supplyProductResponse.setName(product.getName());
									supplyProductResponse.setArticle(supplyBoxProducts.getArticle().toString());
									supplyProductResponse.setVendorCode(product.getVendorCode());
									supplyProductResponse.setBoxBarCode(supplyBox.getVendorCode().toString());
									supplyProductResponse.setStoreAddress(supply.getKaspiStore().getStreet() + ", " + supply.getKaspiStore().getApartment());
									supplyProductResponse.setBoxTypeName(supplyBox.getBoxType().getName());
									supplyProductsRes.add(supplyProductResponse);
								})
				);

		return supplyProductsRes;
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

	private void processSupplyBoxes(Supply supply, Map<Long, SupplyReportResponse> reportMap) {
		supply.getSupplyBoxes().forEach(supplyBox -> {
			var supplyBoxProducts = supplyBox.getSupplyBoxProducts();
			supplyBoxProducts.forEach(product -> processSupplyBoxProduct(product, reportMap));
		});
	}

	private void processSupplyBoxProduct(SupplyBoxProducts supplyBoxProduct, Map<Long, SupplyReportResponse> reportMap) {
		var product = supplyBoxProduct.getProduct();
		var productId = product.getId();

		var report = reportMap.computeIfAbsent(productId, id -> SupplyReportResponse.builder()
				.productName(product.getName())
				.productArticle(supplyBoxProduct.getArticle())
				.countOfProductDeclined(0L)
				.countOfProductAccepted(0L)
				.countOfProductPending(0L)
				.build());

		updateReportCounts(supplyBoxProduct, report);
	}

	private void updateReportCounts(SupplyBoxProducts supplyBoxProduct, SupplyReportResponse report) {
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
