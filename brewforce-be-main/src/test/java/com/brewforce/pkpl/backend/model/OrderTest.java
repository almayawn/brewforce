package com.brewforce.pkpl.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
    }

    @Test
    void addMenuItem_WithValidMenuAndQuantity_ShouldAddItemAndUpdateTotalHarga() {
        // Mock menu
        Menu menu = mock(Menu.class);
        when(menu.isDeleted()).thenReturn(false);
        when(menu.getNamaMenu()).thenReturn("Nasi Goreng");
        when(menu.getHargaMenu()).thenReturn(20000);
        when(menu.getStok()).thenReturn(10);

        // Add menu item
        order.addMenuItem(menu, 2);

        // Assertions
        assertEquals(1, order.getOrderMenuItems().size());
        assertEquals(40000L, order.getTotalHarga());
        verify(menu, times(1)).setStok(8); // Stok berkurang
    }

    @Test
    void addMenuItem_WithDeletedMenu_ShouldThrowException() {
        // Mock menu
        Menu menu = mock(Menu.class);
        when(menu.isDeleted()).thenReturn(true);
        when(menu.getNamaMenu()).thenReturn("Nasi Goreng");

        // Add menu item and expect exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            order.addMenuItem(menu, 2);
        });

        assertTrue(exception.getMessage().contains("sudah dihapus dan tidak dapat dipesan"));
    }

    @Test
    void addMenuItem_WithZeroOrNegativeQuantity_ShouldThrowException() {
        // Mock menu
        Menu menu = mock(Menu.class);
        when(menu.isDeleted()).thenReturn(false);
        when(menu.getNamaMenu()).thenReturn("Nasi Goreng");

        // Add menu item with zero quantity
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            order.addMenuItem(menu, 0);
        });
        assertTrue(exception.getMessage().contains("Jumlah menu harus lebih dari 0."));

        // Add menu item with negative quantity
        exception = assertThrows(IllegalArgumentException.class, () -> {
            order.addMenuItem(menu, -1);
        });
        assertTrue(exception.getMessage().contains("Jumlah menu harus lebih dari 0."));
    }

    @Test
    void addMenuItem_WithInsufficientStock_ShouldThrowException() {
        // Mock menu
        Menu menu = mock(Menu.class);
        when(menu.isDeleted()).thenReturn(false);
        when(menu.getNamaMenu()).thenReturn("Nasi Goreng");
        when(menu.getStok()).thenReturn(1);

        // Add menu item and expect exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            order.addMenuItem(menu, 2);
        });

        assertTrue(exception.getMessage().contains("Stok menu Nasi Goreng tidak mencukupi"));
    }
}