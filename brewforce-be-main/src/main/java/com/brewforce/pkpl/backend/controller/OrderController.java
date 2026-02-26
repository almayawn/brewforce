package com.brewforce.pkpl.backend.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.brewforce.pkpl.backend.dto.response.BaseResponseDTO;
import com.brewforce.pkpl.backend.dto.response.OrderItemResponse;
import com.brewforce.pkpl.backend.dto.response.OrderResponse;
import com.brewforce.pkpl.backend.model.Order;
import com.brewforce.pkpl.backend.model.OrderMenu;
import com.brewforce.pkpl.backend.security.JwtUtils;
import com.brewforce.pkpl.backend.service.OrderService;

@Controller
@RequestMapping("api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private JwtUtils jwtUtils;

    @CrossOrigin("*")
    @PreAuthorize("hasRole('PEMBELI') or hasRole('KASIR')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            String role = jwtUtils.getRoleFromJwtToken(tokenWithoutBearer);

            Order order = orderService.getOrderById(id);
            if (order == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order dengan ID " + id + " tidak ditemukan");
            }

            if (role.equals("PEMBELI")) {
                String username = jwtUtils.getUsernameJwtToken(tokenWithoutBearer);
                if (!order.getUsername().equals(username)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anda tidak memiliki akses ke pesanan ini");
                }
            }

            return ResponseEntity.ok(convertToDto(order));

        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getReason());
            }else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    @CrossOrigin("*")
    @PreAuthorize("hasRole('PEMBELI')")
    @PostMapping("/")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization") String token) {
        try {
            if (token == null || token.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            if (payload == null || !payload.containsKey("menuItems")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Menu items are required");
            }

            List<Map<String, Object>> menuItems;
            try {
                menuItems = (List<Map<String, Object>>) payload.get("menuItems");

                if (menuItems == null || menuItems.isEmpty()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Menu items list cannot be empty");
                }
            } catch (ClassCastException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid menu items format");
            }

            List<UUID> menuIds = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();

            for (Map<String, Object> item : menuItems) {
                if (!item.containsKey("menuId") || !item.containsKey("quantity")) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Each menu item must have menuId and quantity");
                }

                UUID menuId = UUID.fromString(item.get("menuId").toString());
                Integer quantity = Integer.parseInt(item.get("quantity").toString());

                menuIds.add(menuId);
                quantities.add(quantity);
            }

            String username = jwtUtils.getUsernameJwtToken(tokenWithoutBearer);
            Order newOrder = orderService.createOrder(menuIds, quantities, username);

            // Convert to DTO
            OrderResponse response = convertToDto(newOrder);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getReason());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An unexpected error occurred: " + e.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    private OrderResponse convertToDto(Order order) {
        List<OrderItemResponse> items = new ArrayList<>();

        for (OrderMenu orderMenuItem : order.getOrderMenuItems()) {
            OrderItemResponse itemResponse = OrderItemResponse.builder()
                    .id(orderMenuItem.getId())
                    .menuId(orderMenuItem.getMenu().getIdMenu())
                    .menuName(orderMenuItem.getMenu().getNamaMenu())
                    .quantity(orderMenuItem.getQuantity())
                    .price(orderMenuItem.getMenu().getHargaMenu())
                    .build();

            items.add(itemResponse);
        }

        return OrderResponse.builder()
                .idOrder(order.getIdOrder())
                .status(order.getStatus())
                .totalHarga(order.getTotalHarga())
                .username(order.getUsername())
                .createdDateTime(order.getCreatedAt())
                .items(items)
                .build();
    }

    @CrossOrigin("*")
    @PreAuthorize("hasRole('KASIR')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatusOrder(@RequestBody Order updateOrder,
            @RequestHeader(value = "Authorization") String token) {
        var baseResponseDTO = new BaseResponseDTO<Order>();
        try {
            if (token == null || token.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            // Validasi bahwa status yang direquest tidak boleh CANCELLED
            if ("CANCELLED".equals(updateOrder.getStatus())) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Tidak dapat mengubah status menjadi 'CANCELLED'");
                baseResponseDTO.setData(null);
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }

            Order updatedOrder = orderService.updateStatusOrder(updateOrder);
            
            if (updatedOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Pesanan dengan ID " + updateOrder.getIdOrder() + " tidak ditemukan");
            }
            return new ResponseEntity<>(convertToDto(updatedOrder), HttpStatus.OK);

        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
            }
        }
    }

    @CrossOrigin("*")
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('PEMBELI')")
    public ResponseEntity<?> getOrdersByUsername(@RequestHeader(value = "Authorization") String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak ditemukan");
        }

        String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
        }

        String username = jwtUtils.getUsernameJwtToken(tokenWithoutBearer);

        // Get orders for the authenticated user
        List<Order> orders = orderService.getOrdersByUsername(username);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(orderResponses);
    }

    @CrossOrigin("*")
    @PreAuthorize("hasRole('KASIR')")
    @GetMapping("")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(value = "statuses", required = false) List<String> statuses) {
        var baseResponseDTO = new BaseResponseDTO<Order>();
        try {

            List<Order> orders;

            if (statuses != null && !statuses.isEmpty()) {
                orders = orderService.getOrdersByStatuses(statuses);
            } else {
                orders = orderService.getAllOrders();
            }

            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToDto)
                    .toList();

            return ResponseEntity.ok(orderResponses);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("An error occurred: " + e.getMessage());
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin("*")
    @PutMapping("/cancel")
    @PreAuthorize("hasRole('PEMBELI')")
    public ResponseEntity<?> cancelOrder(
            @RequestBody Order updateOrder,
            @RequestHeader(value = "Authorization") String token) {
        var baseResponseDTO = new BaseResponseDTO<Order>();
        try {
            // Validasi token
            if (token == null || token.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak ditemukan");
            }

            String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            // Validasi username: harus pemilik pesanan
            String username = jwtUtils.getUsernameJwtToken(tokenWithoutBearer);
            Order order = orderService.getOrderById(updateOrder.getIdOrder());

            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Pesanan dengan ID " + updateOrder.getIdOrder() + " tidak ditemukan");
            }

            if (!order.getUsername().equals(username)) {
                baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                baseResponseDTO.setMessage("Anda hanya dapat membatalkan pesanan milik Anda sendiri");
                baseResponseDTO.setData(null);
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
            }

            // Validasi bahwa status yang direquest adalah CANCELLED
            if (!"CANCELLED".equals(updateOrder.getStatus())) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Status harus 'CANCELLED' untuk membatalkan pesanan");
                baseResponseDTO.setData(null);
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }

            Order updatedOrder = orderService.updateStatusOrder(updateOrder);

            // Return response
            OrderResponse response = convertToDto(updatedOrder);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

}
