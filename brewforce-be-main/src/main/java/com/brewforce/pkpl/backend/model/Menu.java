package com.brewforce.pkpl.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "menu")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_menu")
    private UUID idMenu;

    @Column(name = "nama_menu", nullable = false)
    private String namaMenu;

    @Column(name = "deskripsi_menu", nullable = false)
    private String deskripsiMenu;

    @Column(name = "harga_menu", nullable = false)
    private int hargaMenu;

    @Min(value = 0, message = "Stok tidak boleh kurang dari 0")
    @Column(name = "stok",  nullable = false)
    private int stok;

    private boolean isDeleted;

    @Column(name = "foto_path",nullable = false)
    private String fotoPath;
   
    @OneToMany(mappedBy = "menu")
    private List<OrderMenu> orderMenuItems = new ArrayList<>();
}