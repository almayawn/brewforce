package com.brewforce.pkpl.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.brewforce.pkpl.backend.model.Menu;
import com.brewforce.pkpl.backend.model.Order;
import com.brewforce.pkpl.backend.model.OrderMenu;
import com.brewforce.pkpl.backend.repository.MenuRepository;
import com.brewforce.pkpl.backend.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
        }
 
        @Transactional
        public Order createOrder(List<UUID> menuIds, List<Integer> quantities, String username) {
            List<Order> existingOrders = orderRepository.findByUsername(username);
            for (Order existingOrder : existingOrders) {
                String status = existingOrder.getStatus();
                if (!status.equals("CANCELLED") && !status.equals("COMPLETED")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User already has an active order with status: " + status);
                }
        }

        Order order = new Order();
        
        for (int i = 0; i < menuIds.size(); i++) {
            Menu menu = menuRepository.findById(menuIds.get(i))
                .orElseThrow(() -> new RuntimeException("Menu not found"));

            order.addMenuItem(menu, quantities.get(i));
        }
        order.setUsername(username);

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(UUID id) {
        for (Order order : orderRepository.findAll()) {
            if (order.getIdOrder().equals(id)) {
                return order;
            }
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return orderRepository.findAll();
        }

        return orderRepository.findByStatusIn(statuses);
    }

    @Override
    @Transactional
    public Order updateStatusOrder(Order updateOrder) {
        // Ambil order berdasarkan ID
        Order order = getOrderById(updateOrder.getIdOrder());
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Order with ID " + updateOrder.getIdOrder() + " not found");
        }

        // Validasi status baru berdasarkan status saat ini
        String currentStatus = order.getStatus();
        String newStatus = updateOrder.getStatus();
    
        // Simpan status sebelumnya untuk pengecekan pembatalan
        boolean isCancellation = !currentStatus.equals("CANCELLED") && newStatus.equals("CANCELLED");
    
        // Validasi perubahan status
        switch (currentStatus) {
            case "AWAITING_PAYMENT":
                if (!newStatus.equals("PREPARING") && !newStatus.equals("CANCELLED")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid status transition from AWAITING_PAYMENT to " + newStatus);
                }
                break;
            case "PREPARING":
                if (!newStatus.equals("READY")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid status transition from PREPARING to " + newStatus);
                }
                break;
            case "READY":
                if (!newStatus.equals("COMPLETED")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid status transition from READY to " + newStatus);
                }
                break;
            case "COMPLETED":
            case "CANCELLED":
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order with status " + currentStatus + " cannot be updated");
            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected current status: " + currentStatus);
        }
    
        // Update status
        order.setStatus(newStatus);
        
        // Jika order dibatalkan, kembalikan stok menu
        if (isCancellation) {
            restoreMenuStock(order);
        }
        
        orderRepository.save(order);
        return order;
    }

    /**
     * Mengembalikan stok menu ketika order dibatalkan
     */
    private void restoreMenuStock(Order order) {
        // Untuk setiap item dalam order
        for (OrderMenu orderMenuItem : order.getOrderMenuItems()) {
            Menu menu = orderMenuItem.getMenu();
            int quantityToRestore = orderMenuItem.getQuantity();
            
            // Tambahkan kembali stok
            menu.setStok(menu.getStok() + quantityToRestore);
            
            // Simpan perubahan pada menu
            menuRepository.save(menu);
        }
    }

    @Override
    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsername(username);
    }

}
