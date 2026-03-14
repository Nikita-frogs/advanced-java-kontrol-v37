package service;

import domain.*;
import exceptions.*;

import java.io.IOException;
import java.math.BigDecimal;

public class StandardOrderProcessor extends OrderProcessorTemplate {
    public StandardOrderProcessor(OrderRepository orderRepository) {
        super(orderRepository);
    }

    @Override
    protected void validate(Order order) {
        OrderItem[] items = order.getItems();
        if (items.length > 5) {
            log.warn("Validation failed: Max 5 items allowed.");
            throw new ValidationException("Maximum 5 items allowed.");
        }
        for (OrderItem item : items) {
            if (item.getQuantity() > 20) {
                log.warn("Validation failed: Quantity exceeds 20 for an item.");
                throw new ValidationException("Item quantity cannot exceed 20.");
            }
        }
        order.setStatus(OrderStatus.VALIDATED);
    }

    @Override
    protected void calculateTotal(Order order) {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            BigDecimal lineTotal = item.getPrice().getAmount().multiply(new BigDecimal(item.getQuantity()));
            sum = sum.add(lineTotal);
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (sum.compareTo(new BigDecimal("15000")) > 0) {
            discount = discount.add(new BigDecimal("0.08"));
        }
        if (order.isVip()) {
            discount = discount.add(new BigDecimal("0.02"));
        }

        BigDecimal finalAmount = sum.subtract(sum.multiply(discount));
        order.setTotalAmount(new Money(finalAmount));
        log.info("Calculated total: {}", finalAmount);
    }

    @Override
    protected void confirmShipment(Order order) throws ShipmentConfirmationException {
        boolean infrastructureIsDown = order.getId().equals("FAIL_CONFIRM");
        if (infrastructureIsDown) {
            throw new ShipmentConfirmationException("External delivery service is down", new IOException("Timeout"));
        }

        if (order.getStatus() == OrderStatus.SHIPPED) {
            log.info("Shipment confirmed by delivery service.");
            order.setStatus(OrderStatus.DELIVERED);
        } else {
            throw new ValidationException("Cannot confirm shipment. Not shipped yet.");
        }
    }
}
