package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import model.GmailConnection;
import model.Order;
import model.PrefContract;
import service.PrinterService;
import view.OnItemClickListener;
import view.OnPrintListener;
import view.OrderView;
import view.UpdateListener;

public class OrderController implements UpdateListener, OnItemClickListener, OnPrintListener {
    private List<Order> orders;
    private OrderView view;

    public OrderController() {
        orders = new ArrayList<>();
        view = new OrderView(this, this);
        setButtonListeners(view);
        view.pack();
        view.setVisible(true);

        Preferences prefs = PrefContract.getPrefs();
        String username = prefs.get(PrefContract.KEY_USERNAME, PrefContract.DEF_USERNAME);
        String password = prefs.get(PrefContract.KEY_PASSWORD, PrefContract.DEF_PASSWORD);
        String SENDER_ADDRESS = prefs.get(PrefContract.KEY_SENDER, PrefContract.DEF_SENDER);

//        String username = "zhimingweng778@gmail.com";
//        String password = "hgohwrzzcskwpshu";
//        String SENDER_ADDRESS = "Jimmy Ong <zhimingweng778@gmail.com>";

//      String username = "freshwok0505@gmail.com";
//      String password = "hwowjpnfiouspgxs";
//      String SENDER_ADDRESS = "DoorDash <orders@doordash.com>";

        // Load 3 dictionaries

        GmailConnection connection = new GmailConnection(this, SENDER_ADDRESS);
        // Call method fetch
        connection.fetch(username, password);
    }

    private void setButtonListeners(OrderView view) {
        view.getSettingButton().addActionListener(new SettingActionListener(view));
        view.getAboutButton().addActionListener(new AboutActionListener(view));
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
            PrinterService service = new PrinterService();
            service.print(newOrder);
        }
    }

    @Override
    public void clearList() {
        orders.clear();
        view.clearListData();
    }

    @Override
    public void onItemClick(int index) {
        view.setDetailText(orders.get(index));
    }

    @Override
    public void onPrint(int index) {
        PrinterService service = new PrinterService();
        service.print(orders.get(index));
    }

    
    @Override
    public void setNoConnectionVisibility(boolean visible) {
        view.setNoConnectionVisibility(visible);
    }

    public static void main(String[] args) {
        new OrderController();
    }

    private static class SettingActionListener implements ActionListener {
        private JFrame view;

        public SettingActionListener(JFrame view) {
            super();
            this.view = view;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SettingController settingController = new SettingController(view);
        }

    }

    private static class AboutActionListener implements ActionListener {
        private JFrame view;

        public AboutActionListener(JFrame view) {
            super();
            this.view = view;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(view,
                    "<html><h2>Print DoorDash Order</h2><p>Version 1.0.0</p>"
                            + "<p>License Key: J4L2-****-****-****</p><p>Author: Jimmy</p></html>",
                    "About", JOptionPane.PLAIN_MESSAGE);
        }

    }

}
