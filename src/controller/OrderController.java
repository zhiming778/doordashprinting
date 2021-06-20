package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import model.GmailConnection;
import model.Order;
import model.PrefContract;
import service.PrinterService;
import view.OrderView;
import view.OrderView.OnOrderListener;
import view.SettingDialog;
import view.UpdateListener;

public class OrderController implements UpdateListener, OnOrderListener {
    private List<Order> orders;
    private OrderView view;
    
    public OrderController(OrderView view) {
        this.view = view;
        orders = new ArrayList<>();
        view.pack();
        view.setVisible(true);

        Preferences prefs = Preferences.userNodeForPackage(PrefContract.class);
        String username = prefs.get(PrefContract.KEY_USERNAME, PrefContract.DEF_USERNAME);
        String password = prefs.get(PrefContract.KEY_PASSWORD, PrefContract.DEF_PASSWORD);
        String senderAddress = prefs.get(PrefContract.KEY_SENDER, PrefContract.DEF_SENDER);
//        String username = "zhimingweng778@gmail.com";
//        String password = "hgohwrzzcskwpshu";
//        String SENDER_ADDRESS = "Jimmy Ong <zhimingweng778@gmail.com>";

//      String username = "freshwok0505@gmail.com";
//      String password = "hwowjpnfiouspgxs";
//      String SENDER_ADDRESS = "DoorDash <orders@doordash.com>";

        // Load 3 dictionaries

        GmailConnection connection = new GmailConnection();
        // Call method fetch
        connection.fetch(username, password, senderAddress,(UpdateListener)this);
    }
    public void setOnOrderListener() {
        view.setOnOrderListener((OnOrderListener)this);
    }

    @Override
    public void update(Order newOrder, boolean ifPrint) {
        orders.add(newOrder);
        int len = orders.size();
        String[] data = new String[len];
        for (int i = 0; i < len; i++) {
            data[i] = orders.get(i).getName();
        }
        view.setListData(data);
        if (ifPrint) {
            Preferences prefs = Preferences.userNodeForPackage(PrefContract.class);
            String printerName = prefs.get(PrefContract.KEY_PRINTER, PrefContract.DEF_PRINTER);
            PrinterService service = new PrinterService(printerName);
            service.print(newOrder);
        }
    }

    @Override
    public void clearList() {
        orders.clear();
        view.clearListData();
    }

    @Override
    public void itemClick(int index) {
        view.setDetailText(orders.get(index));
    }

    @Override
    public void buttonClick(int actionType, int index) {
        switch (actionType) {
        case OrderView.ACTION_PRINT:
            Preferences prefs = Preferences.userNodeForPackage(PrefContract.class);
            String printerName = prefs.get(PrefContract.KEY_PRINTER, PrefContract.DEF_PRINTER);
            PrinterService service = new PrinterService(printerName);
            service.print(orders.get(index));
            break;
        case OrderView.ACTION_ABOUT:
            JOptionPane.showMessageDialog(view,
                    "<html><h2>Print DoorDash Order</h2><p>Version 1.0.0</p>"
                            + "<p>License Key: J4L2-****-****-****</p><p>Developer: Zhiming</p></html>",
                    "About", JOptionPane.PLAIN_MESSAGE);
            break;
        case OrderView.ACTION_SETTING:
            SettingController settingController = new SettingController(new SettingDialog(view));
            settingController.setOnSettingListener();
            break;
        default:
            break;
        }
        
    }

    
    @Override
    public void setNoConnectionVisibility(boolean visible) {
        view.setNoConnectionVisibility(visible);
    }

    public static void main(String[] args) {
        OrderController orderController = new OrderController(new OrderView());
        orderController.setOnOrderListener();
    }

}
