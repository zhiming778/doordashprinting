package view;
import model.Order;

public interface UpdateListener {
    void update(Order newOrder, boolean ifPrint);
}
