package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConverterTest {
    private static Converter converter;
    private static String doc1, doc2, doc3;
    private static PDFTextStripperByArea stripper;
    private static PDDocument document;

    @BeforeAll
    static void setUpForClass() {
        DictionaryLoader loader = new DictionaryLoader();
        Map<String, String> dishes = loader.load("dishes.txt");
        Map<String, String> required = loader.load("required.txt");
        Map<String, String> choices = loader.load("choices.txt");
        converter = new Converter(dishes, required, choices);
        doc1 = "Order Number: 8849d2d9\r\n" + "Customer Order Size Pickup Time\r\n"
                + "Jennifer K. 2 items Today at 6:55 PM\r\n" + "1-(855) 973-1040 Jun 19, 2021\r\n"
                + "Payment: Pre-Paid\r\n" + "Qty. Item Price Subtotal\r\n"
                + "1x 100. Mushroom Egg Foo Young (in Egg Foo Young) $13.35 $13.35\r\n"
                + "•  Rice choice Veg fried rice (+ $4.00)\r\n"
                + "1x 18. Steamed or Fried Dumplings (8) (in Appetizers) $6.95 $6.95\r\n"
                + "•  Steamed or Fried Choice Steamed\r\n" + "~ End of Order ~\r\n"
                + "2x Total Items Subtotal: $20.30\r\n" + "+ Tax (8.020%): $1.63\r\n" + "Total: $21.93";
        doc2 = "Order Number: \r\n" + "Customer Order Size Pickup Time\r\n" + "Adam N. 4 items Today at 7:09 PM\r\n"
                + "1-(855) 973-1040 Jun 19, 2021\r\n" + "Payment: Pre-Paid\r\n" + "Qty. Item Price Subtotal\r\n"
                + "1x 18. Steamed or Fried Dumplings (8) (in Appetizers) $6.95 $6.95\r\n"
                + "•  Steamed or Fried Choice Steamed\r\n"
                + "1x 22. Cream Cheese Wontons (6) (in Appetizers) $5.45 $5.45\r\n"
                + "1x 41. Chicken Lo Mein (in Lo Mein) $6.85 $6.85\r\n" + "•  Lo Mein Size Choice Small\r\n"
                + "1x S2. Sesame Chicken (in Chef's Suggestions) $11.50 $11.50\r\n" + "•  Spicy Choice Mild\r\n"
                + "~ End of Order ~\r\n" + "4x Total Items Subtotal: $30.75\r\n" + "+ Tax (8.020%): $1.67\r\n"
                + "Total: $32.42\r\n" + "Total: $32.42";
        doc3 = "Order Number: 6f672049\r\n" + "Customer Order Size Pickup Time\r\n" + " 3 items Today at 9:10 PM\r\n"
                + "1-(855) 973-1040 Jun 19, 2021\r\n" + "Payment: Pre-Paid\r\n" + "Qty. Item Price Subtotal\r\n"
                + "1x 22. Cream Cheese Wontons (6) (in Appetizers) $5.45 $5.45\r\n"
                + "1x 58. Chicken w. Cashew Nuts (in Poultry) $11.55 $11.55\r\n" + "•  Poultry Size Choice Small\r\n"
                + "•  Rice choice Chicken fried rice (+ $4.00)\r\n"
                + "1x 69. Lemon Chicken (in Poultry) $13.85 $13.85\r\n" + "•  Rice choice Fried rice (+ $3.00)\r\n"
                + "~ End of Order ~\r\n" + "3x Total Items Subtotal: $30.85\r\n" + "+ Tax (8.020%): $2.48\r\n"
                + "Total: $33.33";
        document = mock(PDDocument.class);
        when(document.getNumberOfPages()).thenReturn(1);
        PDRectangle pdRectangle = mock(PDRectangle.class);
        when(pdRectangle.getWidth()).thenReturn(200.0f);
        when(pdRectangle.getHeight()).thenReturn(200.0f);
        PDPage page = mock(PDPage.class);
        when(document.getPage(0)).thenReturn(page);
        when(page.getBBox()).thenReturn(pdRectangle);
        stripper = mock(PDFTextStripperByArea.class);
    }

    @Test
    void testNullDictionary() {
        DictionaryLoader loader = new DictionaryLoader();
        Map<String, String> dishes = loader.load("dishes.txt");
        Map<String, String> required = loader.load("required.txt");
        Map<String, String> choices = loader.load("choices.txt");
        assertThrows(NullPointerException.class, () -> new Converter(null, null, null));
        assertThrows(NullPointerException.class, () -> new Converter(dishes, required, null));
        assertThrows(NullPointerException.class, () -> new Converter(null, required, choices));
        assertThrows(NullPointerException.class, () -> new Converter(dishes, null, choices));
    }

    @Test
    void testNullPDDocument() {
        assertThrows(NullPointerException.class, () -> converter.convert(null, null));
        PDDocument document = mock(PDDocument.class);
        assertThrows(NullPointerException.class, () -> converter.convert(document, null));
        PDFTextStripperByArea stripper = mock(PDFTextStripperByArea.class);
        assertThrows(NullPointerException.class, () -> converter.convert(null, stripper));
    }

    @Test
    void testConvert1() {
        when(stripper.getTextForRegion("region_1")).thenReturn(doc1);
        assertEquals(new Order("8849d2d9", "Jennifer K.", "6:55 PM", 2, 21.93, new String[] { "蘑菇蓉蛋", "????", "水饺" }),
                converter.convert(document, stripper));
        when(stripper.getTextForRegion("region_1")).thenReturn(doc2);
        assertEquals(
                new Order("N/A", "Adam N.", "7:09 PM", 4, 32.42, new String[] { "水饺", "其", "小鸡捞面", "芝麻鸡", "  不辣" }),
                converter.convert(document, stripper));
        when(stripper.getTextForRegion("region_1")).thenReturn(doc3);
        assertEquals(
                new Order("6f672049", "N/A", "9:10 PM", 3, 33.33,
                        new String[] { "其", "小腰果鸡", "????", "柠檬鸡", "????" }),
                converter.convert(document, stripper));
    }

}
