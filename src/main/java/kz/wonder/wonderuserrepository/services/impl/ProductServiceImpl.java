package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.entities.Product;
import kz.wonder.wonderuserrepository.entities.ProductPrice;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.ProductPriceRepository;
import kz.wonder.wonderuserrepository.repositories.ProductRepository;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.Utils.getStringFromExcelCell;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
	private final ProductRepository productRepository;
	private final ProductPriceRepository productPriceRepository;
	private final KaspiCityRepository kaspiCityRepository;


	@Override
	@Transactional
	public List<ProductResponse> processExcelFile(MultipartFile excelFile, String keycloakUserId) {
		try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
			List<ProductResponse> productResponses = new ArrayList<>();

			if (workbook.getNumberOfSheets() == 0)
				throw new IllegalArgumentException("File must have at least one page!");
			Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
			Iterator<Row> rowIterator = sheet.iterator();
			if (rowIterator.hasNext()) {
				rowIterator.next();
				rowIterator.next();
				rowIterator.next();
			}
			if (!rowIterator.hasNext())
				throw new IllegalArgumentException("Send file by requirements!!");
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				String vendorCode = getStringFromExcelCell(row.getCell(0));
				if (vendorCode.isEmpty())
					continue;

				Product product = processProduct(row, keycloakUserId, vendorCode);
				processProductPrices(product, row);

				productResponses.add(this.mapToResponse(product));
			}
			return productResponses;
		} catch (IllegalStateException e) {
			log.error("IllegalStateException: ", e);
			throw new IllegalArgumentException("File process failed");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Product processProduct(Row row, String keycloakUserId, String vendorCode) {
		Product product = productRepository
				.findByVendorCodeAndKeycloakId(vendorCode, keycloakUserId)
				.orElse(new Product());
		product.setVendorCode(vendorCode);
		product.setName(row.getCell(1).getStringCellValue());
		product.setLink(row.getCell(2).getStringCellValue());
		product.setEnabled(Boolean.parseBoolean(row.getCell(3).getStringCellValue()));
		product.setKeycloakId(keycloakUserId);
		product.setDeleted(false);
		return productRepository.save(product);
	}

	private void processProductPrices(Product product, Row row) {
		final String CITY_ALMATY = "Алматы";
		final String CITY_ASTANA = "Астана";

		var cityAlmaty = getCityByName(CITY_ALMATY);
		var cityAstana = getCityByName(CITY_ASTANA);

		var priceAtAlmaty = processProductPrice(product, cityAlmaty, row.getCell(4).getNumericCellValue());
		var priceAtAstana = processProductPrice(product, cityAstana, row.getCell(5).getNumericCellValue());

		product.setPrices(new ArrayList<>());
		product.getPrices().add(priceAtAlmaty);
		product.getPrices().add(priceAtAstana);
	}

	private KaspiCity getCityByName(String cityName) {
		return kaspiCityRepository.findByName(cityName)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));
	}

	private ProductPrice processProductPrice(Product product, KaspiCity city, Double priceValue) {
		var productPrice = productPriceRepository.findByProductAndKaspiCityName(product, city.getName())
				.orElse(new ProductPrice(city, product, priceValue));
		productPrice.setKaspiCity(city);
		productPrice.setPrice(priceValue);
		productPrice.setUpdatedAt(LocalDateTime.now());
		return productPriceRepository.save(productPrice);
	}

	@Override
	public List<ProductResponse> getProductsByKeycloakId(String keycloakUserId) {
		return productRepository.findAllByKeycloakId(keycloakUserId)
				.stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	private ProductResponse mapToResponse(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.enabled(product.isEnabled())
				.name(product.getName())
				.vendorCode(product.getVendorCode())
				.keycloakUserId(product.getKeycloakId())
				.prices(
						product.getPrices().stream().map(
								productPrice ->
										ProductResponse.ProductPriceResponse.builder()
												.price(productPrice.getPrice())
												.cityName(productPrice.getKaspiCity().getName())
												.build()
						).collect(Collectors.toList())
				)

				.build();
	}

}
