package model;

import java.awt.geom.Rectangle2D;
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
    private final Map<String, String> dishes, required, choice;
    private final String END_SIGN = "~ End of Order ~";
    private final String START_SIGN = "Qty";
    private final String notFound = "????";
    private final String specialInstruction = "Special instructions";
    private final String PLACE_HOLDER = "#";
    private final Pattern itemPattern, choicePattern;
    private int startIndex, endIndex;

    public Converter(Map<String, String> dishes, Map<String, String> required, Map<String, String> choice) {
        if (dishes == null || required == null || choice == null)
            throw new NullPointerException("dictionary can't be null");
        itemPattern = Pattern.compile("(\\d{1,2})x([^\\(]*)"); // group 2
        choicePattern = Pattern.compile("Choice ([^\\(]+)"); // group 1
        this.dishes = dishes;
        this.required = required;
        this.choice = choice;
    }

    private String[] init(PDDocument document, PDFTextStripperByArea stripper) {
        if (document == null || stripper == null)
            throw new NullPointerException("document or stripper can't be null.");
        StringBuilder sb = new StringBuilder();
        try {
            int numOfPages = document.getNumberOfPages();
            final PDRectangle pdRectangle = document.getPage(0).getBBox();
            Rectangle2D rect = pdRectangleToRectangle2D(pdRectangle);
            stripper.setSortByPosition(true);
            stripper.addRegion("region_1", rect);
            for (int i = 0; i < numOfPages; i++) {
                stripper.extractRegions(document.getPage(i));
                sb.append(stripper.getTextForRegion("region_1"));
            }
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startIndex = -1;
        endIndex = -1;
//        System.out.println(sb.toString());
        return sb.toString().split("\r\n");
    }

    public Order convert(PDDocument document, PDFTextStripperByArea stripper) {
        String[] rawDoc = init(document, stripper);
        int len = format(rawDoc);
        String[] items = getItems(rawDoc);
        String date = getDate(rawDoc);
        String id = getId(rawDoc);
        String name = getName(rawDoc);
        int numOfItems = getNumberOfItems(rawDoc);
        double total = getTotal(rawDoc, len);
        return new Order(id, name, date, numOfItems, total, items);
    }

    private Rectangle2D pdRectangleToRectangle2D(PDRectangle pdRectangle) {
        float width = pdRectangle.getWidth();
        float height = pdRectangle.getHeight();
        return new Rectangle2D.Float(0, 0, width, height - 132);
    }

    private String removePrice(String origin) {
        int index = origin.indexOf('$');
        if (index != -1)
            return origin.substring(0, index).trim();
        else
            return origin;
    }

    private int format(String[] rawDoc) {
        int len = 0;
        final Pattern itemPattern = Pattern.compile("^\\d+x");
        for (int i = 0; i < rawDoc.length; i++) {
            // outside item part
            if (startIndex == -1 || endIndex != -1) {
                rawDoc[len++] = rawDoc[i];
                if (rawDoc[i].contains(START_SIGN))
                    startIndex = len;
            }
            // within item part
            else if (Character.isDigit(rawDoc[i].charAt(0))) {
                Matcher matcher = itemPattern.matcher(rawDoc[i]);
                String priceRemoved = removePrice(rawDoc[i]);
                if (matcher.find()) {
                    rawDoc[len++] = priceRemoved;
                } else {
                    rawDoc[len - 1] = rawDoc[len - 1] + " " + priceRemoved;
                }
            }
            // choice or required
            else if (rawDoc[i].startsWith("\u2022")) {
                rawDoc[len++] = removePrice(rawDoc[i]);
            }
            // end of order
            else if (rawDoc[i].equals(END_SIGN)) {
                endIndex = len;
                rawDoc[len++] = rawDoc[i];
            }
            // break lines within item part, details of special instruction
            else {
                rawDoc[len - 1] = rawDoc[len - 1] + " " + removePrice(rawDoc[i]);
            }
        }
        return len;
    }

    private String[] getItems(String[] rawDoc) {
        boolean isRequired = false;
        List<String> items = new ArrayList<String>();
        for (int i = startIndex; i < endIndex; i++) {
            // main item
            if (Character.isDigit(rawDoc[i].charAt(0))) {
                Matcher matcher = itemPattern.matcher(rawDoc[i]);
                String item;
                if (matcher.find()) {
                    String engItem = matcher.group(2).trim();
                    if ((item = dishes.get(engItem)) == null) { // corresponding chinese item doesn't exist
                        item = engItem;
                    } else {
                        int num = Integer.parseInt(matcher.group(1));
                        if (num != 1) {
                            item = num + "x " + item;
                        }
                        isRequired = item.contains(PLACE_HOLDER) ? true : false;
                    }
                } else { // regex couldn't capture the item name
                    item = notFound;
                }
                items.add(item);
            }
            // choice or required
            else if (rawDoc[i].startsWith("\u2022")) {
                Matcher matcher = choicePattern.matcher(rawDoc[i]);
                if (matcher.find()) {
                    String engItem = matcher.group(1).trim();
                    String item;
                    if (isRequired) {
                        isRequired = false;
                        if ((item = required.get(engItem)) == null) {
                            item = engItem;
                        }
                        String lastItem = items.get(items.size() - 1);
                        lastItem = lastItem.replace(PLACE_HOLDER, item);
                        items.set(items.size() - 1, lastItem);
                    } else {
                        if ((item = choice.get(engItem)) == null)
                            item = engItem;
                        items.add(item);
                    }
                } else {
                    if (rawDoc[i].contains(specialInstruction)) {
                        items.add(rawDoc[i].replace(specialInstruction, ""));
                    } else {
                        String item = notFound;
                        if (isRequired) {
                            isRequired = false;
                            String lastItem = items.get(items.size() - 1);
                            lastItem = lastItem.replace(PLACE_HOLDER, item);
                            items.set(items.size() - 1, lastItem);
                        } else {
                            items.add(item);
                        }
                    }
                }
            } else if (rawDoc[i].equals(END_SIGN))
                break;
        }
        return items.toArray(new String[items.size()]);
    }

    private double getTotal(String[] rawDoc, int len) {
        final String TOTAL = "Total:";
        final Pattern totalPattern = Pattern.compile("\\$(\\d+.\\d+)");
        double total = -1;
        for (int i = len - 1; i > endIndex; i--) {
            if (rawDoc[i].contains(TOTAL)) {
                Matcher matcher = totalPattern.matcher(rawDoc[i]);
                if (matcher.find()) {
                    total = Double.parseDouble(matcher.group(1));
                    break;
                }
            }
        }
        return total;
    }

    // return -1 if it doesn't match any number
    private String getId(String[] rawDoc) {
        final Pattern idPattern = Pattern.compile(": ([\\d\\w]+)");
        Matcher matcher = idPattern.matcher(rawDoc[0]);
        if (matcher.find())
            return matcher.group(1);
        return "N/A";
    }

    private String getName(String[] rawDoc) {
        Pattern idPattern = Pattern.compile("^\\S+\\s\\S+");
        Matcher idMatcher = idPattern.matcher(rawDoc[2]);
        if (idMatcher.find())
            return idMatcher.group();
        return "N/A";
    }

    private String getDate(String[] rawDoc) {
        final Pattern datePattern = Pattern.compile("at (\\d{1,2}:\\d{1,2} [A|P]M)");
        String date = null;
        for (int i = 2; i < startIndex; i++) {
            Matcher matcher = datePattern.matcher(rawDoc[i]);
            if (matcher.find()) {
                date = matcher.group(1);
                break;
            }
        }
        if (date == null)
            return "N/A";
        return date;
    }

    private int getNumberOfItems(String[] rawDoc) {
        final Pattern numPattern = Pattern.compile("(\\d+) items");
        int num = -1;
        for (int i = 2; i < startIndex; i++) {
            Matcher matcher = numPattern.matcher(rawDoc[i]);
            if (matcher.find()) {
                num = Integer.parseInt(matcher.group(1));
                break;
            }
        }
        return num;
    }
}
