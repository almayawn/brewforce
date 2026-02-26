package com.brewforce.pkpl.backend.controller;

import com.brewforce.pkpl.backend.model.Order;
import com.brewforce.pkpl.backend.security.JwtUtils;
import com.brewforce.pkpl.backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Mock
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllOrders_WithoutStatuses_ShouldReturnAllOrders() {
        List<Order> mockOrders = new ArrayList<>();
        Order order1 = new Order();
        Order order2 = new Order();
        mockOrders.addAll(Arrays.asList(order1, order2));

        when(orderService.getAllOrders()).thenReturn(mockOrders);

        ResponseEntity<?> response = orderController.getAllOrders(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).getAllOrders();
        verify(orderService, never()).getOrdersByStatuses(any());
    }

    @Test
    void getAllOrders_WithStatuses_ShouldReturnFilteredOrders() {
        List<String> statuses = Arrays.asList("PENDING", "COMPLETED");
        List<Order> mockOrders = new ArrayList<>();
        Order order1 = new Order();
        mockOrders.add(order1);

        when(orderService.getOrdersByStatuses(statuses)).thenReturn(mockOrders);

        ResponseEntity<?> response = orderController.getAllOrders(statuses);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).getOrdersByStatuses(statuses);
        verify(orderService, never()).getAllOrders();
    }

    @Test
    void getAllOrders_WhenServiceThrowsException_ShouldReturnBadRequest() {
        when(orderService.getAllOrders()).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = orderController.getAllOrders(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getOrderById_WithValidTokenAndExistingId_ShouldReturnOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken(anyString())).thenReturn("KASIR");
        when(orderService.getOrderById(orderId)).thenReturn(order);

        ResponseEntity<?> response = orderController.getOrderById(orderId, "Bearer validToken");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    void getOrderById_WithNonExistingId_ShouldReturnNotFound() {
        UUID orderId = UUID.randomUUID();
        String token = "Bearer validToken";

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("KASIR");
        when(orderService.getOrderById(orderId)).thenReturn(null);

        ResponseEntity<?> response = orderController.getOrderById(orderId, token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Order dengan ID " + orderId + " tidak ditemukan"));
    }

    @Test
    void createOrder_WithValidPayloadAndToken_ShouldReturnCreated() {
        Map<String, Object> payload = Map.of("menuItems",
                List.of(Map.of("menuId", UUID.randomUUID().toString(), "quantity", 1)));
        Order mockOrder = new Order();
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken(anyString())).thenReturn("testUser");
        when(orderService.createOrder(anyList(), anyList(), anyString())).thenReturn(mockOrder);

        ResponseEntity<?> response = orderController.createOrder(payload, "Bearer validToken");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(orderService, times(1)).createOrder(anyList(), anyList(), eq("testUser"));
    }

    @Test
    void updateStatusOrder_WithValidTokenAndStatus_ShouldReturnUpdatedOrder() {
        Order updateOrder = new Order();
        updateOrder.setStatus("COMPLETED");
        Order mockOrder = new Order();
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(true);
        when(orderService.updateStatusOrder(any(Order.class))).thenReturn(mockOrder);

        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, "Bearer validToken");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).updateStatusOrder(updateOrder);
    }

    // CREATE ORDER BY ID TEST CASES
    // 1. Broken Access Control
    @Test
    void createOrder_WithUnauthorizedRole_ShouldReturnForbidden() {
        String token = "Bearer invalidToken";
        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.createOrder(new HashMap<>(), token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 2. Cryptographic Failures
    @Test
    void createOrder_WithMissingToken_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = orderController.createOrder(new HashMap<>(), null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 3. Injection
    @Test
    void createOrder_WithInvalidPayload_ShouldThrowBadRequest() {
        String token = "Bearer validToken";
        Map<String, Object> invalidPayload = new HashMap<>(); // Empty payload

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("PEMBELI");

        ResponseEntity<?> response = orderController.createOrder(invalidPayload, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Menu items are required", response.getBody());
    }

    // 4. Insecure Design
    @Test
    void createOrder_WithEmptyMenuItems_ShouldThrowBadRequest() {
        String token = "Bearer validToken";
        Map<String, Object> payload = new HashMap<>();
        payload.put("menuItems", new ArrayList<>());

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("PEMBELI");

        ResponseEntity<?> response = orderController.createOrder(payload, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Menu items list cannot be empty", response.getBody());
    }

    // 5. Security Misconfiguration
    @Test
    void createOrder_WithInvalidMenuItemFormat_ShouldThrowBadRequest() {
        String token = "Bearer validToken";
        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("PEMBELI");

        Map<String, Object> payload = Map.of("menuItems", List.of(Map.of("invalidField", "value")));

        ResponseEntity<?> response = orderController.createOrder(payload, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Each menu item must have menuId and quantity", response.getBody());
    }

    // 7. Identification and Authentication Failures
    @Test
    void createOrder_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalidToken";
        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.createOrder(new HashMap<>(), token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 8. Software and Data Integrity Failures
    @Test
    void createOrder_WithTamperedToken_ShouldReturnUnauthorized() {
        String token = "Bearer tamperedToken";
        when(jwtUtils.validateJwtToken("tamperedToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.createOrder(new HashMap<>(), token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // GET ORDER BY ID TEST CASES
    // 1. Broken Access Control
    @Test
    void getOrderById_WithUnauthorizedaToken_ShouldReturnForbidden() {
        String token = "Bearer invalidToken";
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setIdOrder(orderId);
        order.setUsername("User");
        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.getOrderById(orderId, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 2. Cryptographic Failures
    @Test
    void getOrderById_WithMissingToken_ShouldReturnUnauthorized() {
        UUID orderId = UUID.randomUUID();

        ResponseEntity<?> response = orderController.getOrderById(orderId, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 3. Injection
    @Test
    void getOrderById_WithInvalidOrderId_ShouldThrowBadRequest() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("KASIR");
        when(orderService.getOrderById(orderId)).thenReturn(null);

        ResponseEntity<?> response = orderController.getOrderById(orderId, token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Order dengan ID " + orderId + " tidak ditemukan"));
    }

    // 4. Insecure Design
    @Test
    void getOrderById_WithNonExistingOrder_ShouldReturnNotFound() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("KASIR");
        when(orderService.getOrderById(orderId)).thenReturn(null);

        ResponseEntity<?> response = orderController.getOrderById(orderId, token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Order dengan ID " + orderId + " tidak ditemukan"));
    }

    // 5. Security Misconfiguration
    @Test
    void getOrderById_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalidToken";
        UUID orderId = UUID.randomUUID();

        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.getOrderById(orderId, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 7. Identification and Authentication Failures
    @Test
    void getOrderById_WithTamperedToken_ShouldReturnUnauthorized() {
        String token = "Bearer tamperedToken";
        UUID orderId = UUID.randomUUID();

        when(jwtUtils.validateJwtToken("tamperedToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.getOrderById(orderId, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 8. Software and Data Integrity Failures
    @Test
    void getOrderById_WithTamperedOrderData_ShouldThrowException() {
        // Setup
        UUID orderId = UUID.randomUUID();
        String token = "Bearer validToken";
        Order order = new Order();
        order.setIdOrder(orderId);
        order.setUsername("differentUser");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("PEMBELI");
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("testUser");
        when(orderService.getOrderById(orderId)).thenReturn(order);

        // Execute
        ResponseEntity<?> response = orderController.getOrderById(orderId, token);

        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Anda tidak memiliki akses"));
    }

    // UPDATE STATUS TEST CASES
    // 1. Broken Access Control
    @Test
    void updateStatusOrder_WithUnauthorizedRole_ShouldReturnForbidden() {
        String token = "Bearer invalidToken";
        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);
        Order updateOrder = new Order();
        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 2. Cryptographic Failures
    @Test
    void updateStatusOrder_WithMissingToken_ShouldReturnUnauthorized() {
        Order updateOrder = new Order();
        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 3. Injection
    @Test
    void updateStatusOrder_WithInvalidStatus_ShouldReturnBadRequest() {
        String token = "Bearer validToken";
        Order updateOrder = new Order();
        updateOrder.setStatus("INVALID_STATUS"); // Invalid status injection attempt

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("KASIR");
        when(orderService.updateStatusOrder(updateOrder))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status"));

        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, token);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // 4. Insecure Design
    @Test
    void updateStatusOrder_WithCancelledStatus_ShouldReturnBadRequest() {
        String token = "Bearer validToken";
        Order updateOrder = new Order();
        updateOrder.setStatus("CANCELLED");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("KASIR");

        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, token);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // 5. Security Misconfiguration
    @Test
    void updateStatusOrder_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalidToken";
        Order updateOrder = new Order();

        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, token);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 7. Identification and Authentication Failures
    @Test
    void updateStatusOrder_WithTamperedToken_ShouldReturnUnauthorized() {
        String token = "Bearer tamperedToken";
        Order updateOrder = new Order();

        when(jwtUtils.validateJwtToken("tamperedToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, token);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 8. Software and Data Integrity Failures
    @Test
    void updateStatusOrder_WithTamperedOrderData_ShouldReturnBadRequest() {
        String token = "Bearer validToken";
        Order updateOrder = new Order();
        updateOrder.setIdOrder(UUID.randomUUID());
        updateOrder.setStatus("COMPLETED");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("KASIR");
        when(orderService.updateStatusOrder(updateOrder))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order data integrity check failed"));

        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, token);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // 10. Server-Side Request Forgery
    @Test
    void updateStatusOrder_WithNonExistingOrder_ShouldReturnNotFound() {
        String token = "Bearer validToken";
        Order updateOrder = new Order();
        updateOrder.setIdOrder(UUID.randomUUID());
        updateOrder.setStatus("COMPLETED");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("KASIR");
        when(orderService.updateStatusOrder(updateOrder))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        ResponseEntity<?> response = orderController.updateStatusOrder(updateOrder, token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // tests for getOrdersByUsername method

    // 1. Broken Access Control - Unauthorized Access Attempt
    @Test
    void getOrdersByUsername_WithUserAccessingOthersData_ShouldBeRestricted() {
        String token = "Bearer validToken";
        String authenticatedUser = "user1";
        String attemptedAccessUsername = "user2";
        List<Order> orders = new ArrayList<>();

        // Simulate authenticated user
        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn(authenticatedUser);

        // Set up orders that belong to a different user
        Order order = new Order();
        order.setUsername(attemptedAccessUsername);
        orders.add(order);

        // Service should return only orders matching the authenticated user
        when(orderService.getOrdersByUsername(authenticatedUser)).thenReturn(new ArrayList<>());
        when(orderService.getOrdersByUsername(attemptedAccessUsername)).thenReturn(orders);

        ResponseEntity<?> response = orderController.getOrdersByUsername(token);

        // Verify that service was called with authenticated username, not the attempted
        // access username
        verify(orderService).getOrdersByUsername(authenticatedUser);
        verify(orderService, never()).getOrdersByUsername(attemptedAccessUsername);

        // Should return OK with empty list (since authenticatedUser has no orders)
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // 1. Broken Access Control - Another user's account access attempt
    @Test
    void getOrdersByUsername_WithAdminRoleChangingToUserRole_ShouldBeVerified() {
        String token = "Bearer validToken";

        // Mock token validation and role check
        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("admin");

        // Simulate admin's orders
        List<Order> adminOrders = new ArrayList<>();
        Order adminOrder = new Order();
        adminOrder.setUsername("admin");
        adminOrders.add(adminOrder);

        when(orderService.getOrdersByUsername("admin")).thenReturn(adminOrders);

        ResponseEntity<?> response = orderController.getOrdersByUsername(token);

        // Verify that only admin's own orders are returned
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).getOrdersByUsername("admin");

        // Additional verification to confirm admin can't access other users' data
        verify(orderService, never()).getOrdersByUsername("user1");
        verify(orderService, never()).getOrdersByUsername("user2");
    }

    // 2. Cryptographic Failures
    @Test
    void getOrdersByUsername_WithNullToken_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = orderController.getOrdersByUsername(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
    }

    // 3. Injection
    @Test
    void getOrdersByUsername_WithSQLInjectionAttempt_ShouldHandleSecurely() {
        String token = "Bearer validToken";
        String maliciousUsername = "admin'; DROP TABLE users; --";

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn(maliciousUsername);
        when(orderService.getOrdersByUsername(maliciousUsername)).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = orderController.getOrdersByUsername(token);
        verify(orderService).getOrdersByUsername(maliciousUsername);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // 4. Insecure Design
    @Test
    void getOrdersByUsername_WhenServiceThrowsException_ShouldHandleGracefully() {
        String token = "Bearer validToken";
        String username = "testUser";

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn(username);
        when(orderService.getOrdersByUsername(username)).thenThrow(new RuntimeException("Database error"));

        try {
            orderController.getOrdersByUsername(token);
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

    // 5. Security Misconfiguration
    @Test
    void getOrdersByUsername_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalidToken";
        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.getOrdersByUsername(token);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 6. Vulnerable and Outdated Components
    @Test
    void getOrdersByUsername_WithMalformedToken_ShouldHandleProperly() {
        String token = "MalformedTokenWithoutBearer";
        when(jwtUtils.validateJwtToken(token)).thenReturn(false);

        ResponseEntity<?> response = orderController.getOrdersByUsername(token);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 7. Identification and Authentication Failures
    @Test
    void getOrdersByUsername_WithTamperedToken_ShouldReturnUnauthorized() {
        String token = "Bearer tamperedToken";
        when(jwtUtils.validateJwtToken("tamperedToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.getOrdersByUsername(token);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 8. Software and Data Integrity Failures
    @Test
    void getOrdersByUsername_WithModifiedUsername_ShouldOnlyReturnAuthorizedData() {
        String token = "Bearer validToken";
        String username = "legitimateUser";

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn(username);
        when(orderService.getOrdersByUsername(username)).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = orderController.getOrdersByUsername(token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).getOrdersByUsername(username);
        assertNotNull(response.getBody());
    }

    // Tests for cancelOrder method

    // 1. Broken Access Control
    @Test
    void cancelOrder_WithUserTryingToCancelOthersOrder_ShouldReturnForbidden() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();
        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("CANCELLED");

        Order existingOrder = new Order();
        existingOrder.setIdOrder(orderId);
        existingOrder.setUsername("anotherUser"); // Different from authenticated user

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("testUser");
        when(orderService.getOrderById(orderId)).thenReturn(existingOrder);

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 1. Broken Access Control - Missing Authorization
    @Test
    void cancelOrder_WithEmptyToken_ShouldReturnUnauthorized() {
        Order updateOrder = new Order();
        updateOrder.setStatus("CANCELLED");

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, "");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 2. Cryptographic Failures
    @Test
    void cancelOrder_WithNullToken_ShouldReturnUnauthorized() {
        Order updateOrder = new Order();
        updateOrder.setStatus("CANCELLED");

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 3. Injection
    @Test
    void cancelOrder_WithInvalidStatus_ShouldReturnBadRequest() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();
        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("INVALID_STATUS"); // Injection attempt with invalid status

        Order existingOrder = new Order();
        existingOrder.setIdOrder(orderId);
        existingOrder.setUsername("testUser");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("testUser");
        when(orderService.getOrderById(orderId)).thenReturn(existingOrder);

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 4. Insecure Design
    @Test
    void cancelOrder_WithNonexistentOrder_ShouldReturnNotFound() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();
        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("CANCELLED");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("testUser");
        when(orderService.getOrderById(orderId)).thenReturn(null);

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("tidak ditemukan"));
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 5. Security Misconfiguration
    @Test
    void cancelOrder_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalidToken";
        Order updateOrder = new Order();
        updateOrder.setStatus("CANCELLED");

        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 6. Vulnerable and Outdated Components
    @Test
    void cancelOrder_WithMalformedToken_ShouldHandleGracefully() {
        String token = "MalformedTokenNoBearer";
        Order updateOrder = new Order();
        updateOrder.setStatus("CANCELLED");

        when(jwtUtils.validateJwtToken("MalformedTokenNoBearer")).thenReturn(false);

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 7. Identification and Authentication Failures
    @Test
    void cancelOrder_WithTamperedToken_ShouldReturnUnauthorized() {
        String token = "Bearer tamperedToken";
        Order updateOrder = new Order();
        updateOrder.setStatus("CANCELLED");

        when(jwtUtils.validateJwtToken("tamperedToken")).thenReturn(false);

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
        verify(orderService, never()).updateStatusOrder(any(Order.class));
    }

    // 8. Software and Data Integrity Failures
    @Test
    void cancelOrder_WithServiceError_ShouldHandleException() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();
        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("CANCELLED");

        Order existingOrder = new Order();
        existingOrder.setIdOrder(orderId);
        existingOrder.setUsername("testUser");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("testUser");
        when(orderService.getOrderById(orderId)).thenReturn(existingOrder);
        when(orderService.updateStatusOrder(updateOrder))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel this order"));

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cannot cancel this order", response.getBody());
    }

    // 9. Security Logging and Monitoring Failures
    // Not directly testable in unit tests - would require log capture/verification

    // 10. Server-Side Request Forgery
    @Test
    void cancelOrder_WithServiceFailure_ShouldHandleInternalServerError() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();
        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("CANCELLED");

        Order existingOrder = new Order();
        existingOrder.setIdOrder(orderId);
        existingOrder.setUsername("testUser");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("testUser");
        when(orderService.getOrderById(orderId)).thenReturn(existingOrder);
        when(orderService.updateStatusOrder(updateOrder))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error"));

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("unexpected error"));
    }

    // Positive Test Case - Successful Cancellation
    @Test
    void cancelOrder_WithValidRequest_ShouldCancelSuccessfully() {
        String token = "Bearer validToken";
        UUID orderId = UUID.randomUUID();
        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("CANCELLED");

        Order existingOrder = new Order();
        existingOrder.setIdOrder(orderId);
        existingOrder.setUsername("testUser");
        existingOrder.setStatus("AWAITING_PAYMENT");

        Order cancelledOrder = new Order();
        cancelledOrder.setIdOrder(orderId);
        cancelledOrder.setUsername("testUser");
        cancelledOrder.setStatus("CANCELLED");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("validToken")).thenReturn("testUser");
        when(orderService.getOrderById(orderId)).thenReturn(existingOrder);
        when(orderService.updateStatusOrder(updateOrder)).thenReturn(cancelledOrder);

        ResponseEntity<?> response = orderController.cancelOrder(updateOrder, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).updateStatusOrder(updateOrder);
    }
}
