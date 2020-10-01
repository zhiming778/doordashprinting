import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.DictionaryLoader;
import model.Order;
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
        view = new OrderView("Print DoorDash Orders", this, this);
        view.pack();
        view.setVisible(true);
       
        String username = "freshwok0505@gmail.com";
        String password = "hwowjpnfiouspgxs";
        String SENDER_ADDRESS = "DoorDash <orders@doordash.com>";
     
//        String username = "zhimingweng778@gmail.com";
//        String password = "hgohwrzzcskwpshu";
//        String SENDER_ADDRESS = "Jimmy Ong <zhimingweng778@gmail.com>";
        
        // Load 3 dictionaries
        DictionaryLoader loader = new DictionaryLoader();
        Map<String, String> dishes = loader.load("dishes.txt");
        Map<String, String> required = loader.load("required.txt");
        Map<String, String> choice = loader.load("choice.txt");
        GmailConnection connection = new GmailConnection(this, SENDER_ADDRESS);
        // Call method fetch
        connection.fetch(username, password, dishes, required, choice);
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
    public void onItemClick(int index) {
        view.setDetailText(orders.get(index));
    }

    @Override
    public void onPrint(int index) {
        PrinterService service = new PrinterService();
        service.print(orders.get(index));
    }

    public static void main(String[] args) {
        new OrderController();
    }
}
