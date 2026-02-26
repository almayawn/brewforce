package com.brewforce.pkpl.backend.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.brewforce.pkpl.backend.model.Order;

@Service
public interface OrderService {
    Order createOrder(List<UUID> menuIds, List<Integer> quantities, String username);
    List<Order> getAllOrders();
    Order getOrderById(UUID id);
    List<Order> getOrdersByStatuses(List<String> statuses);
    Order updateStatusOrder(Order updateOrder);
    List<Order> getOrdersByUsername(String username);
}
