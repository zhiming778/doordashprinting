package model;

public class Order {
    private long id;
    private String name;
    private String date;
    private int numOfItems;
    private double total;
    private String[] items;

    public Order(long id, String name, String date, int numOfItems, double total, String[] items) {
        super();
        this.id = id;
        this.name = name;
        this.date = date;
        this.numOfItems = numOfItems;
        this.total = total;
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumOfItems() {
        return numOfItems;
    }

    public void setNumOfItems(int numOfItems) {
        this.numOfItems = numOfItems;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String[] getItems() {
        return items;
    }

    public void setItems(String[] items) {
        this.items = items;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id + "\n");
        sb.append(name + "\n");
        sb.append(date + "\n");
        sb.append(numOfItems + "\n");
        sb.append(String.format("%.2f", total) + "\n");
        for (String item : items) {
            sb.append(item + "\n");
        }
        return sb.toString();
    }
}
