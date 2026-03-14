package service;

import domain.Order;
import domain.OrderStatus;

import exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OrderProcessorTemplate {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final OrderRepository orderRepository;

    protected OrderProcessorTemplate(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public final void process(String orderId, PaymentMethod paymentMethod) {
        log.info("Starting processing for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found"));

        try {
            validate(order);
            calculateTotal(order);
            pay(order, paymentMethod);
            ship(order);

            try {
                confirmShipment(order);
            } catch (ShipmentConfirmationException e) {
                throw new AppException("Failed to confirm shipment infrastructure", e);
            }

            complete(order);
        } catch (AppException e) {
            log.error("Unexpected/Business failure during processing: {}", e.getMessage(), e);
            order.setStatus(OrderStatus.FAILED);
            throw e;
        }
    }

    protected abstract void validate(Order order);
    protected abstract void calculateTotal(Order order);

    protected void pay(Order order, PaymentMethod paymentMethod) {
        log.info("Processing payment for amount: {}", order.getTotalAmount());
        paymentMethod.pay(order.getTotalAmount());
        order.setStatus(OrderStatus.PAID);
    }

    protected void ship(Order order) {
        log.info("Shipping order...");
        order.setStatus(OrderStatus.SHIPPED);
    }

    protected abstract void confirmShipment(Order order) throws ShipmentConfirmationException;

    protected void complete(Order order) {
        log.info("Order successfully completed!");
    }
}
