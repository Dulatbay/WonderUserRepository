package kz.wonder.wonderuserrepository.services.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfWriter;
import kz.wonder.wonderuserrepository.services.BarcodeService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

@Service
public class BarcodeServiceImpl implements BarcodeService {
    @Override
    public MultipartFile generateProductBarcode(String productName, String productArticle, String boxVendorCode, String shopName) {
        try {
            // Prepare text for the PDF
            String barcodeText = productArticle;
            String additionalText = productName + "\n" + boxVendorCode + "\n" + shopName;

            // Generate PDF with barcode
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Generate Barcode
            Barcode128 barcode128 = new Barcode128();
            barcode128.setCode(barcodeText);
            barcode128.setCodeType(Barcode128.CODE128);
            var cb = writer.getDirectContent();
            Image barcodeImage = barcode128.createImageWithBarcode(cb, null, null);
            barcodeImage.setAlignment(Element.ALIGN_CENTER);
            document.add(barcodeImage);

            // Add text below the barcode
            Paragraph paragraph = new Paragraph(additionalText, new Font(Font.FontFamily.HELVETICA, 12));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);

            document.close();

            var filename = productArticle + ".xml";

            return new MockMultipartFile(filename, filename, "text/xml", baos.toByteArray());
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
