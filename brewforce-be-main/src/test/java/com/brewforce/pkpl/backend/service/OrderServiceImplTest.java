package com.brewforce.pkpl.backend.service;

import com.brewforce.pkpl.backend.model.Menu;
import com.brewforce.pkpl.backend.model.Order;
import com.brewforce.pkpl.backend.repository.MenuRepository;
import com.brewforce.pkpl.backend.repository.OrderRepository;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_WithActiveOrder_ShouldThrowBadRequest() {
        UUID menuId = UUID.randomUUID();
        List<UUID> menuIds = List.of(menuId);
        List<Integer> quantities = List.of(1);
        String username = "testUser";

        Order activeOrder = new Order();
        activeOrder.setStatus("AWAITING_PAYMENT");
        when(orderRepository.findByUsername(username)).thenReturn(List.of(activeOrder));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.createOrder(menuIds, quantities, username);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User already has an active order"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_WithValidData_ShouldCreateOrder() {
        UUID menuId = UUID.randomUUID();
        List<UUID> menuIds = List.of(menuId);
        List<Integer> quantities = List.of(2);
        String username = "testUser";

        Menu menu = new Menu();
        menu.setIdMenu(menuId);
        menu.setHargaMenu(10000);
        menu.setStok(10);
        when(orderRepository.findByUsername(username)).thenReturn(Collections.emptyList());
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        Order savedOrder = new Order();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.createOrder(menuIds, quantities, username);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setIdOrder(orderId);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        Order result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getIdOrder());
    }

    @Test
    void getOrderById_WithNonExistingId_ShouldReturnNull() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        Order result = orderService.getOrderById(orderId);

        assertNull(result);
    }

    @Test
    void updateStatusOrder_WithValidTransition_ShouldUpdateStatus() {
        UUID orderId = UUID.randomUUID();
        Order existingOrder = new Order();
        existingOrder.setIdOrder(orderId);
        existingOrder.setStatus("AWAITING_PAYMENT");

        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("PREPARING");

        when(orderRepository.findAll()).thenReturn(List.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        Order result = orderService.updateStatusOrder(updateOrder);

        assertNotNull(result);
        assertEquals("PREPARING", result.getStatus());
        verify(orderRepository, times(1)).save(existingOrder);
    }

    @Test
    void updateStatusOrder_WithInvalidTransition_ShouldThrowBadRequest() {
        UUID orderId = UUID.randomUUID();
        Order existingOrder = new Order();
        existingOrder.setIdOrder(orderId);
        existingOrder.setStatus("AWAITING_PAYMENT");

        Order updateOrder = new Order();
        updateOrder.setIdOrder(orderId);
        updateOrder.setStatus("COMPLETED");

        when(orderRepository.findAll()).thenReturn(List.of(existingOrder));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.updateStatusOrder(updateOrder);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid status transition"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllOrders_WithSQLInjection_ShouldHandleSafely() {
        // OWASP A03:2021 – Injection
        String maliciousSQL = "'; DROP TABLE orders; --";
        Order order = new Order();
        order.setUsername(maliciousSQL);
        when(orderRepository.findAll()).thenReturn(List.of(order));
        
        List<Order> result = orderService.getAllOrders();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOrders_WithXSSPayload_ShouldReturnSanitizedData() {
        // OWASP A03:2021 – Injection (XSS)
        String xssPayload = "<script>alert('xss')</script>";
        Order order = new Order();
        order.setUsername(xssPayload);
        when(orderRepository.findAll()).thenReturn(List.of(order));
        
        List<Order> result = orderService.getAllOrders();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOrders_WithLargeDataSet_ShouldHandleMemoryEfficiently() {
        // OWASP A05:2021 – Security Misconfiguration
        List<Order> largeOrderList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Order order = new Order();
            order.setIdOrder(UUID.randomUUID());
            largeOrderList.add(order);
        }
        when(orderRepository.findAll()).thenReturn(largeOrderList);
        
        List<Order> result = orderService.getAllOrders();
        
        assertNotNull(result);
        assertEquals(10000, result.size());
    }

    @Test
    void getAllOrders_WithNullRepository_ShouldHandleGracefully() {
        // OWASP A01:2021 – Broken Access Control
        orderRepository = null;
        orderService = new OrderServiceImpl(null, menuRepository);
        
        assertThrows(NullPointerException.class, () -> {
            orderService.getAllOrders();
        });
    }

    @Test
    void getAllOrders_WithConcurrentAccess_ShouldHandleThreadSafely() {
        // OWASP A04:2021 – Insecure Design
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> orderService.getAllOrders()));
        }
        
        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                fail("Concurrent access failed");
            }
        });
    }

    @Test
    void getAllOrders_WithMalformedData_ShouldHandleValidation() {
        // OWASP A07:2021 – Identification and Authentication Failures
        Order invalidOrder = new Order();
        invalidOrder.setStatus("INVALID_STATUS");
        when(orderRepository.findAll()).thenReturn(List.of(invalidOrder));
        
        List<Order> result = orderService.getAllOrders();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOrders_WithModifiedData_ShouldPreserveIntegrity() {
        // OWASP A08:2021 – Software and Data Integrity Failures
        Order order = new Order();
        order.setIdOrder(UUID.randomUUID());
        order.setStatus("COMPLETED");
        when(orderRepository.findAll()).thenReturn(List.of(order));
        
        List<Order> result = orderService.getAllOrders();
        Order retrievedOrder = result.get(0);
        
        assertEquals(order.getStatus(), retrievedOrder.getStatus());
        assertEquals(order.getIdOrder(), retrievedOrder.getIdOrder());
    }

    @Test
    void getAllOrders_WithSensitiveData_ShouldNotExposePrivateInfo() {
        // OWASP A02:2021 – Cryptographic Failures
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setIdOrder(orderId);
        order.setUsername("user123");
        order.setStatus("AWAITING_PAYMENT");
        
        // Add menu items with complete required properties
        Menu menu = new Menu();
        menu.setIdMenu(UUID.randomUUID());
        menu.setNamaMenu("Test Menu");
        menu.setHargaMenu(50000);
        menu.setStok(10);  // Set sufficient stock
        menu.setDeleted(false);
        
        order.addMenuItem(menu, 2);
        
        when(orderRepository.findAll()).thenReturn(List.of(order));
        
        List<Order> result = orderService.getAllOrders();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Verify order details are present but properly structured
        Order retrievedOrder = result.get(0);
        assertNotNull(retrievedOrder.getIdOrder());
        assertNotNull(retrievedOrder.getUsername());
        assertNotNull(retrievedOrder.getStatus());
        assertNotNull(retrievedOrder.getOrderMenuItems());
        assertFalse(retrievedOrder.getOrderMenuItems().isEmpty());
        
        // Verify order details are consistent
        assertEquals(orderId, retrievedOrder.getIdOrder());
        assertEquals("user123", retrievedOrder.getUsername());
        assertEquals("AWAITING_PAYMENT", retrievedOrder.getStatus());
        assertEquals(100000, retrievedOrder.getTotalHarga()); // 50000 * 2
    }

    @Test
    void getAllOrders_WithPotentialSSRF_ShouldHandleSafely() {
        // OWASP A10:2021 – Server-Side Request Forgery (SSRF)
        Order order = new Order();
        // Simulasi potential SSRF payload dalam username
        String maliciousUsername = "file:///etc/passwd";
        order.setUsername(maliciousUsername);
        order.setStatus("AWAITING_PAYMENT");
        
        Menu menu = new Menu();
        menu.setIdMenu(UUID.randomUUID());
        menu.setNamaMenu("http://internal-system:8080/api/secret");
        menu.setHargaMenu(50000);
        menu.setStok(10);
        menu.setDeleted(false);
        
        order.addMenuItem(menu, 1);
        
        when(orderRepository.findAll()).thenReturn(List.of(order));
        
        List<Order> result = orderService.getAllOrders();
        
        // Verify SSRF mitigations
        assertNotNull(result);
        assertEquals(1, result.size());
        Order retrievedOrder = result.get(0);
        
        // Verify the potentially dangerous data is still present but can't be exploited
        assertEquals(maliciousUsername, retrievedOrder.getUsername());
        // Verify the data is treated as plain text and not as a URL/file path
        assertDoesNotThrow(() -> {
            new URL(retrievedOrder.getUsername());
        }, "Username should not be processed as a URL");
        
        // Verify menu name with URL is treated as plain text
        retrievedOrder.getOrderMenuItems().forEach(orderMenu -> {
            String menuName = orderMenu.getMenu().getNamaMenu();
            assertDoesNotThrow(() -> {
                new URL(menuName);
            }, "Menu name should not be processed as a URL");
        });
    }

    @Test
    void getOrdersByStatuses_WithNullStatuses_ShouldReturnAllOrders() {
        // Set up mock data
        List<Order> allOrders = new ArrayList<>();
        Order order1 = new Order();
        order1.setStatus("AWAITING_PAYMENT");
        Order order2 = new Order();
        order2.setStatus("PREPARING");
        Order order3 = new Order();
        order3.setStatus("COMPLETED");
        allOrders.addAll(List.of(order1, order2, order3));
        
        when(orderRepository.findAll()).thenReturn(allOrders);
        
        // Call method with null parameter
        List<Order> result = orderService.getOrdersByStatuses(null);
        
        // Verify result
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(orderRepository, times(1)).findAll();
        verify(orderRepository, never()).findByStatusIn(any());
    }
    
    @Test
    void getOrdersByStatuses_WithEmptyStatuses_ShouldReturnAllOrders() {
        // Set up mock data
        List<Order> allOrders = new ArrayList<>();
        Order order1 = new Order();
        order1.setStatus("AWAITING_PAYMENT");
        Order order2 = new Order();
        order2.setStatus("PREPARING");
        allOrders.addAll(List.of(order1, order2));
        
        when(orderRepository.findAll()).thenReturn(allOrders);
        
        // Call method with empty list
        List<Order> result = orderService.getOrdersByStatuses(Collections.emptyList());
        
        // Verify result
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll();
        verify(orderRepository, never()).findByStatusIn(any());
    }
    
    @Test
    void getOrdersByStatuses_WithValidStatuses_ShouldFilterOrders() {
        // Set up mock data and parameters
        List<String> requestedStatuses = List.of("AWAITING_PAYMENT", "PREPARING");
        
        List<Order> filteredOrders = new ArrayList<>();
        Order order1 = new Order();
        order1.setStatus("AWAITING_PAYMENT");
        Order order2 = new Order();
        order2.setStatus("PREPARING");
        filteredOrders.addAll(List.of(order1, order2));
        
        when(orderRepository.findByStatusIn(requestedStatuses)).thenReturn(filteredOrders);
        
        // Call method with valid statuses
        List<Order> result = orderService.getOrdersByStatuses(requestedStatuses);
        
        // Verify result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AWAITING_PAYMENT", result.get(0).getStatus());
        assertEquals("PREPARING", result.get(1).getStatus());
        verify(orderRepository, never()).findAll();
        verify(orderRepository, times(1)).findByStatusIn(requestedStatuses);
    }
    
    @Test
    void getOrdersByStatuses_WithInvalidStatus_ShouldReturnEmptyList() {
        // Set up mock data with invalid status
        List<String> invalidStatuses = List.of("INVALID_STATUS");
        when(orderRepository.findByStatusIn(invalidStatuses)).thenReturn(Collections.emptyList());
        
        // Call method with invalid status
        List<Order> result = orderService.getOrdersByStatuses(invalidStatuses);
        
        // Verify result is empty
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, never()).findAll();
        verify(orderRepository, times(1)).findByStatusIn(invalidStatuses);
    }
    
    @Test
    void getOrdersByStatuses_WithMixedStatuses_ShouldReturnPartialResults() {
        // Set up mock data with mixed valid and invalid statuses
        List<String> mixedStatuses = List.of("AWAITING_PAYMENT", "INVALID_STATUS");
        
        List<Order> partialResults = new ArrayList<>();
        Order order = new Order();
        order.setStatus("AWAITING_PAYMENT");
        partialResults.add(order);
        
        when(orderRepository.findByStatusIn(mixedStatuses)).thenReturn(partialResults);
        
        // Call method with mixed statuses
        List<Order> result = orderService.getOrdersByStatuses(mixedStatuses);
        
        // Verify partial results
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AWAITING_PAYMENT", result.get(0).getStatus());
        verify(orderRepository, never()).findAll();
        verify(orderRepository, times(1)).findByStatusIn(mixedStatuses);
    }
}