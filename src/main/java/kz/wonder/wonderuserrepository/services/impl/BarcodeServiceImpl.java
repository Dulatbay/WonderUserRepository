package kz.wonder.wonderuserrepository.services.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import kz.wonder.wonderuserrepository.dto.response.SellerSupplyReport;
import kz.wonder.wonderuserrepository.entities.Supply;
import kz.wonder.wonderuserrepository.services.BarcodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.DATE_TIME_FORMATTER;

@Slf4j
@Service
public class BarcodeServiceImpl implements BarcodeService {
    @Override
    public MultipartFile generateBarcode(String barcodeText, List<String> additionalTexts) {
        try {
            Rectangle pageSize = new Rectangle(48 * 2.83465f, 50 * 2.83465f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(pageSize);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();


            BaseFont baseFont = BaseFont.createFont("fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 4);

            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.BOX);
            cell.setPadding(5);

            Barcode128 barcode128 = new Barcode128();
            barcode128.setCode(barcodeText);
            barcode128.setCodeType(Barcode128.CODE128);

            var cb = writer.getDirectContent();
            Image barcodeImage = barcode128.createImageWithBarcode(cb, null, null);
            barcodeImage.setAlignment(Element.ALIGN_CENTER);
            barcodeImage.scaleToFit(100, 20);
            cell.addElement(barcodeImage);


            for (String text : additionalTexts) {
                Paragraph paragraph = new Paragraph(text, font);
                paragraph.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(paragraph);
            }

            table.addCell(cell);
            document.add(table);

            document.close();

            var filename = barcodeText + ".pdf";

            return new MockMultipartFile(filename, filename, "application/pdf", baos.toByteArray());
        } catch (DocumentException | IOException e) {
            log.error("Exception while uploading file: ", e);
            return null;
        }
    }

    public MultipartFile generateSupplyReport(SellerSupplyReport supplyReport) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            BaseFont baseFont = BaseFont.createFont("fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 12);
            Font boldFont = new Font(baseFont, 12, Font.BOLD);

            addSupplyDetails(document, supplyReport, boldFont);

            for (var supplyBox : supplyReport.getSupplyBoxInfo()) {
                addBoxDetails(document, supplyBox, font);
            }

            document.close();
            writer.close();

            String filename = "supply_report_" + supplyReport.getSupplyId() + ".pdf";
            return new MockMultipartFile(filename, filename, "application/pdf", baos.toByteArray());
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addSupplyDetails(Document document, SellerSupplyReport supply, Font font) throws DocumentException {
        document.add(new Paragraph("Дата составления: " + supply.getSupplyCreationDate().format(DATE_TIME_FORMATTER), font));
        document.add(new Paragraph("Выбранное время доставки: " + supply.getSupplySelectedDate().format(DATE_TIME_FORMATTER), font));
        document.add(new Paragraph("Адрес склада: " + supply.getFormattedAddress(), font));
        document.add(new Paragraph("Номер поставки: " + supply.getSupplyId(), font));
        if (supply.getSupplyAcceptanceDate() == null) {
            document.add(new Paragraph("Поставка еще не принята", font));
        } else {
            document.add(new Paragraph("Дата принятия поставки: " + supply.getSupplyAcceptanceDate().format(DATE_TIME_FORMATTER), font));
        }
        document.add(Chunk.NEWLINE);
    }

    private void addBoxDetails(Document document, SellerSupplyReport.SupplyBoxInfo supplyBox, Font font) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(5);

        cell.addElement(new Paragraph("ID: " + supplyBox.getBoxVendorCode(), font));
        cell.addElement(new Paragraph("Тип: " + supplyBox.getBoxName(), font));
        cell.addElement(new Paragraph("Описание: " + supplyBox.getBoxName(), font));
        cell.addElement(new Paragraph("Продуктов внутри: " + supplyBox.getSize(), font));
        cell.addElement(new Paragraph("Продукты:", font));

        for (var product : supplyBox.getProductInfo()) {
            cell.addElement(new Paragraph(product.getProductName() + " - " + product.getProductCount() + " шт.", font));
        }

        table.addCell(cell);
        document.add(table);
        document.add(Chunk.NEWLINE);
    }
}
