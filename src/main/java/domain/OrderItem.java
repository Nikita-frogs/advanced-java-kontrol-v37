package domain;

public class OrderItem {
    private final String name;
    private final int quantity;
    private final Money price;

    public OrderItem(String name, int quantity, Money price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public int getQuantity() { return quantity; }
    public Money getPrice() { return price; }
}
