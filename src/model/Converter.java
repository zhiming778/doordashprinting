package model;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class Converter {
    private final String[] rawDoc;
    private final Map<String, String> dishes, required, choice;
    private final String endOfOrder = "~ End of Order ~";
    private final String notFound = "???????";
    private final String specialInstruction = "Special instructions";
    private final String PLACE_HOLDER = "#";
    private final Pattern itemPattern, choicePattern;

    public Converter(PDDocument document, Map<String, String> dishes, Map<String, String> required,
            Map<String, String> choice) {
        String doc = "";
        try {
            int numOfPages = document.getNumberOfPages();
            PDRectangle pdRectangle = document.getPage(0).getBBox();
            Rectangle2D rect = pdRectangleToRectangle2D(pdRectangle);
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            stripper.addRegion("region_1", rect);
            for (int i = 0; i < numOfPages; i++) {
                stripper.extractRegions(document.getPage(i));
                doc += stripper.getTextForRegion("region_1");
            }
            System.out.println(doc);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        rawDoc = doc.split("\r\n");
        itemPattern = Pattern.compile("(\\d{1,2})x([^\\(]*)"); // group 2
        choicePattern = Pattern.compile("Choice ([^\\(]+)"); // group 1
        this.dishes = dishes;
        this.required = required;
        this.choice = choice;
    }

    private Rectangle2D pdRectangleToRectangle2D(PDRectangle pdRectangle) {
        float width = pdRectangle.getWidth();
        float height = pdRectangle.getHeight();
        return new Rectangle2D.Float(0, 0, width, height - 132);
    }

    public Order convert() {
        long id = getId();
        String name = getName();
        int numOfItems = getNumberOfItems();
        double total = getTotal();
        String[] items = getItems();
        String date = getDate();
        return new Order(id, name, date, numOfItems, total, items);
    }

    private String[] getItems() {
        int startIndex = 6;
        int endIndex = rawDoc.length - 1;
        boolean isRequired = false;
        List<String> items = new ArrayList<String>();
        for (int i = startIndex; i <= endIndex; i++) {
            // main item
            if (Character.isDigit(rawDoc[i].charAt(0))) {
                Matcher matcher = itemPattern.matcher(rawDoc[i]);
                String item;
                if (matcher.find()) {
                    String engItem = matcher.group(2).trim();
                    if ((item = dishes.get(engItem)) == null) { // corresponding chinese item doesn't exist or the item
                                                                // isn't captured
                        Pattern itemExcPattern = Pattern.compile("^[^\\(]+");
                        Matcher itemExcMatcher = itemExcPattern.matcher(rawDoc[i + 1]);
                        if (itemExcMatcher.find()) {
                            engItem = itemExcMatcher.group().trim();
                            if ((item = dishes.get(engItem)) == null) {
                                item = notFound;
                            } else {
                                int num = Integer.parseInt(matcher.group(1));
                                if (num != 1) {
                                    item = num + "x " + item;
                                }
                                i++;
                            }
                        } else
                            item = notFound;
                    } else {
                        int num = Integer.parseInt(matcher.group(1));
                        if (num != 1) {
                            item = num + "x " + item;
                        }
                    }
                    isRequired = item.contains(PLACE_HOLDER) ? true : false;
                } else {
                    item = notFound;
                }
                items.add(item);
            }
            // choice or required
            else if (rawDoc[i].startsWith("\u2022")) {
                Matcher matcher = choicePattern.matcher(rawDoc[i]);
                if (matcher.find()) {
                    String engItem = matcher.group(1).trim();
                    if (isRequired) {
                        isRequired = false;
                        String item;
                        if ((item = required.get(engItem)) != null) {
                            String lastItem = items.get(items.size() - 1);
                            lastItem = lastItem.replace(PLACE_HOLDER, item);
                            items.set(items.size() - 1, lastItem);
                        }
                    } else {
                        String item;
                        if ((item = choice.get(engItem)) == null)
                            item = notFound;
                        items.add(item);
                    }
                } else {
                    if (rawDoc[i].contains(specialInstruction)) {
                        items.add(" "+rawDoc[i++ + 1]);
                    }
                }
            } else if (rawDoc[i].equals(endOfOrder))
                break;
        }
        return items.toArray(new String[items.size()]);
    }

    private double getTotal() {
        final String TOTAL = "Total";
        for (int i = rawDoc.length - 1; i > 0; i--) {
            if (rawDoc[i].startsWith(TOTAL)) {
                Pattern totalPattern = Pattern.compile("\\$(.+)$");
                Matcher matcher = totalPattern.matcher(rawDoc[i]);
                if (matcher.find())
                    return Double.parseDouble(matcher.group(1));
                else
                    return -1;
            }
        }
        return -1;
    }

    // return -1 if it doesn't match any number
    private long getId() {
        Pattern idPattern = Pattern.compile("\\d+");
        Matcher idMatcher = idPattern.matcher(rawDoc[0]);
        if (idMatcher.find())
            return Long.parseLong(idMatcher.group());
        else
            return -1;
    }

    private String getName() {
        Pattern idPattern = Pattern.compile("^\\S+\\s\\S+");
        Matcher idMatcher = idPattern.matcher(rawDoc[2]);
        if (idMatcher.find())
            return idMatcher.group();
        else
            return "N/A";
    }

    private String getDate() {
        Pattern idPattern = Pattern.compile("at (.+)");
        Matcher idMatcher = idPattern.matcher(rawDoc[2]);
        if (idMatcher.find())
            return idMatcher.group(1);
        else
            return "N/A";
    }

    private int getNumberOfItems() {
        Pattern idPattern = Pattern.compile(" (\\d+) items");
        Matcher idMatcher = idPattern.matcher(rawDoc[2]);
        if (idMatcher.find())
            return Integer.parseInt(idMatcher.group(1));
        else
            return -1;
    }

    public static void main(String[] args) {
        PDDocument document;
        try {
            DictionaryLoader loader = new DictionaryLoader();
            Map<String, String> dishes = loader.load("dishes.txt");
            Map<String, String> required = loader.load("required.txt");
            Map<String, String> choice = loader.load("choice.txt");
            document = PDDocument.load(new File("test0.pdf"));
            Converter converter = new Converter(document, dishes, required, choice);
            Order order = converter.convert();
            document.close();
            System.out.println(order.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
