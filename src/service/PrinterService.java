package service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;

import model.Commands;
import model.Order;

public class PrinterService {

    private final String HEADER = "Door Dash", PRINTER_NAME = "receipt";

    public void print(Order order) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        DocPrintJob printJob = null;
        for (PrintService service : services) {
            if (service != null && service.getName().equals(PRINTER_NAME)) {
                printJob = service.createPrintJob();
                break;
            }
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            setChineseModeOn(os);
            printHeader(os);
            printSingleDivider(os);
            printInfo(os, order);
            printSingleDivider(os);
            setMedFontSize(os);
            String[] items = order.getItems();
            final int len = items.length;
            for (int i = 0; i < len; i++) {
                printItem(os, items[i]);
            }
            feedLine(os);
            feedLine(os);
            printTotal(os, order);
            printSingleDivider(os);
            setChineseModeOff(os);
            cutPaper(os);
            if (printJob == null)
                return;
            printJob.print(new SimpleDoc(os.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null),
                    new HashPrintRequestAttributeSet());
        } catch (PrintException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printTotal(ByteArrayOutputStream os, Order order) throws IOException {
        os.write(Commands.RIGHT_ALIGN);
        os.write(("Total: " + order.getTotal()).getBytes("GBK"));
        feedLine(os);
    }

    private void cutPaper(ByteArrayOutputStream os) throws IOException {
        os.write(Commands.CUT_PAPER);
    }

    private void printInfo(ByteArrayOutputStream os, Order order) throws UnsupportedEncodingException, IOException {
        os.write(Commands.CHAR_SIZE_X2);
        os.write(("Order# " + order.getId()).getBytes("GBK"));
        feedLine(os);
        os.write(("数量: " + order.getNumOfItems()).getBytes("GBK"));
        feedLine(os);
        os.write(("名字:  " + order.getName()).getBytes("GBK"));
        feedLine(os);
        os.write(("时间: " + order.getDate()).getBytes("GBK"));
        feedLine(os);
    }

    private void printHeader(ByteArrayOutputStream os) throws UnsupportedEncodingException, IOException {
        os.write(Commands.CENTER_ALIGN);
        setMedFontSize(os);
        os.write(HEADER.getBytes("GBK"));
        feedLine(os);
    }

    private void setSmallFontSize(ByteArrayOutputStream os) throws IOException {
        os.write(Commands.CHAR_SIZE_X1);
    }

    private void setMedFontSize(ByteArrayOutputStream os) throws IOException {
        os.write(Commands.CHAR_SIZE_X2);
    }

    private void printItem(ByteArrayOutputStream os, String text) throws UnsupportedEncodingException, IOException {
        os.write(text.getBytes("GBK"));
        feedLine(os);

    }

    private void setChineseModeOn(ByteArrayOutputStream os) throws IOException {
        os.write(Commands.CHINESE_CHARSET);
        os.write(Commands.CHINESE_MODE_ON);
    }

    private void setChineseModeOff(ByteArrayOutputStream os) throws IOException {
        os.write(Commands.CHINESE_MODE_OFF);
    }

    private void feedLine(ByteArrayOutputStream os) throws IOException {
        os.write(Commands.LF);
    }

    private void printSingleDivider(ByteArrayOutputStream os) throws IOException {
        setSmallFontSize(os);
        os.write(Commands.LEFT_ALIGN);
        for (int i = 0; i < Commands.NUM_OF_LINE_CHAR; i++) {
            os.write(Commands.SINGLE_DIVIDER);
        }
        feedLine(os);
    }

    public static void main(String[] args) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        DocPrintJob printJob = null;
        for (PrintService service : services) {
            System.out.println(service.getName());
            if (service != null) {
                printJob = service.createPrintJob();
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] cs = new byte[] { 0x1B, 0x52, 0x0F };
                    os.write(cs);
                    os.write(Commands.CENTER_ALIGN);
                    os.write(Commands.CHAR_SIZE_X2);
                    os.write("Door Dash".getBytes());
                    os.write(Commands.LF);
                    os.write(Commands.CHAR_SIZE_X1);
                    os.write(Commands.LEFT_ALIGN);
                    for (int i = 0; i < Commands.NUM_OF_LINE_CHAR; i++) {
                        os.write(Commands.SINGLE_DIVIDER);
                    }
                    os.write(Commands.LF);
                    os.write(Commands.CHAR_SIZE_X2);
                    os.write("数量:4 名字： Jimmy".getBytes("GBK"));
                    os.write(Commands.LF);
                    os.write(Commands.CHAR_SIZE_X1);
                    for (int i = 0; i < Commands.NUM_OF_LINE_CHAR; i++) {
                        os.write(Commands.SINGLE_DIVIDER);
                    }
                    os.write(Commands.LF);
                    os.write(Commands.CHAR_SIZE_X2);
                    os.write("测试项目 1".getBytes("GBK"));
                    os.write(Commands.LF);
                    os.write(Commands.LF);
                    os.write("测试项目 2".getBytes("GBK"));
                    os.write(Commands.LF);
                    os.write(Commands.LF);
                    os.write("测试项目 3".getBytes("GBK"));
                    os.write(Commands.LF);
                    os.write(("Printer: " + service.getName()).getBytes("GBK"));
                    os.write(Commands.LF);
                    os.write(Commands.LF);
                    os.write(Commands.CHAR_SIZE_X1);
                    for (int i = 0; i < Commands.NUM_OF_LINE_CHAR; i++) {
                        os.write(Commands.SINGLE_DIVIDER);
                    }
                    os.write(Commands.LF);
                    os.write(Commands.CUT_PAPER);
                    printJob.print(new SimpleDoc(os.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null),
                            new HashPrintRequestAttributeSet());
                } catch (PrintException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }
}
