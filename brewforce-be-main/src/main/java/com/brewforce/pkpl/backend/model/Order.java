package com.brewforce.pkpl.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_order")
    private UUID idOrder;

    @Column(nullable = false)
    @Pattern(
        regexp = "^(AWAITING_PAYMENT|PREPARING|READY|COMPLETED|CANCELLED)$", 
        message = "Role must be one of: AWAITING_PAYMENT, PREPARING, READY, COMPLETED, CANCELLED"
    )
    private String status = "AWAITING_PAYMENT";

    @Column(name = "total_harga", nullable = false)
    private long totalHarga = 0;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderMenu> orderMenuItems = new ArrayList<>();
    
    @Column(name = "username", nullable = false)
    private String username;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Method to add menu item to order
    public void addMenuItem(Menu menu, int quantity) {
        if (menu.isDeleted()) {
            throw new IllegalArgumentException("Menu " + menu.getNamaMenu() + 
                    " sudah dihapus dan tidak dapat dipesan.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Jumlah menu harus lebih dari 0.");
        }
        OrderMenu orderMenuItem = new OrderMenu();
        orderMenuItem.setOrder(this);
        orderMenuItem.setMenu(menu);
        orderMenuItem.setQuantity(quantity);
        
        orderMenuItems.add(orderMenuItem);
        totalHarga += menu.getHargaMenu() * quantity;
        if (menu.getStok() < quantity) {
            throw new IllegalArgumentException("Stok menu " + menu.getNamaMenu() + 
                    " tidak mencukupi. Tersedia: " + menu.getStok() + ", Diminta: " + quantity);
        }
        menu.setStok(menu.getStok() - quantity);
    }
}
