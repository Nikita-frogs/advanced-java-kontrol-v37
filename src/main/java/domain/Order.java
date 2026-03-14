package domain;

import java.math.BigDecimal;
import java.util.Arrays;

public class Order {
    private final String id;
    private final Email customer;
    private final OrderItem[] items;
    private final boolean isVip;
    private Money totalAmount;
    private OrderStatus status;

    public Order(String id, Email customer, OrderItem[] items) {
        this(id, customer, items, false);
    }

    public Order(String id, Email customer, OrderItem[] items, boolean isVip) {
        this.id = id;
        this.customer = customer;
        this.items = items != null ? Arrays.copyOf(items, items.length) : new OrderItem[0];
        this.isVip = isVip;
        this.status = OrderStatus.CREATED;
        this.totalAmount = new Money(BigDecimal.ZERO);
    }

    public String getId() { return id; }
    public OrderItem[] getItems() { return Arrays.copyOf(items, items.length); }
    public boolean isVip() { return isVip; }
    public Money getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Money totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}