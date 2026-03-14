package service;

import domain.Order;

import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(String id);
}