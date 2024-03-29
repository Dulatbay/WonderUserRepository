package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
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
	public void processExcelFile(MultipartFile file, String keycloakUserId) {
		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
			if (workbook.getNumberOfSheets() == 0)
				throw new IllegalArgumentException("File must have at least one page!");
			Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
			Iterator<Row> rowIterator = sheet.iterator();
			if (rowIterator.hasNext()) {
				rowIterator.next();
				rowIterator.next();
				rowIterator.next();
			}

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				String vendorCode = getStringFromExcelCell(row.getCell(0));
				if (vendorCode.isEmpty())
					continue;

				String name = row.getCell(1).getStringCellValue();
				String link = row.getCell(2).getStringCellValue();
				boolean enabled = Boolean.parseBoolean(row.getCell(3).getStringCellValue());
				Double priceAlmaty = row.getCell(4).getNumericCellValue();
				Double priceAstana = row.getCell(5).getNumericCellValue();

				Product product = productRepository
						.findByVendorCodeAndKeycloakId(vendorCode, keycloakUserId)
						.orElse(new Product());

				product.setVendorCode(vendorCode);
				product.setName(name);
				product.setLink(link);
				product.setEnabled(enabled);
				product.setKeycloakId(keycloakUserId);
				product.setDeleted(false);
				product = productRepository.save(product);

				var city = kaspiCityRepository.findByName("Алматы")
						.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));

				ProductPrice price = productPriceRepository.findByProductAndKaspiCityName(product, "Алматы")
						.orElse(new ProductPrice(city, product, priceAlmaty));

				price.setKaspiCity(city);
				price.setPrice(priceAlmaty);
				price.setUpdatedAt(LocalDateTime.now());
				productPriceRepository.save(price);

				city = kaspiCityRepository.findByName("Астана")
						.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));


				price = productPriceRepository.findByProductAndKaspiCityName(product, "Астана")
						.orElse(new ProductPrice(city, product, priceAstana));

				price.setKaspiCity(city);
				price.setPrice(priceAstana);
				price.setUpdatedAt(LocalDateTime.now());
				productPriceRepository.save(price);
			}
		} catch (IllegalStateException e) {
			log.error("IllegalStateException: ", e);
			throw new IllegalArgumentException("File process failed");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ProductResponse> getProductsByKeycloakId(String keycloakUserId) {
		return productRepository.findAllByKeycloakId(keycloakUserId)
				.stream().map(product -> ProductResponse.builder()
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

						.build()).collect(Collectors.toList())
				;
	}

}
