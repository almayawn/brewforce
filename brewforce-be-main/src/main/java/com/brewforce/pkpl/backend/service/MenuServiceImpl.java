package com.brewforce.pkpl.backend.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.brewforce.pkpl.backend.dto.request.UpdateMenuRequest;
import com.brewforce.pkpl.backend.dto.request.UpdateStokRequest;
import com.brewforce.pkpl.backend.dto.response.MenuResponse;
import com.brewforce.pkpl.backend.model.Menu;
import com.brewforce.pkpl.backend.repository.MenuRepository;

@Service
public class MenuServiceImpl implements MenuService {
    @Autowired
    private MenuRepository menuRepository;

    @Override
    public Menu createMenu(Menu menu) {
        // Validate price
        if (menu.getHargaMenu() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Harga menu harus lebih dari 0");
        }

        // Validate stock/quantity
        if (menu.getStok() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stok menu tidak boleh negatif");
        }
        menuRepository.save(menu);
        return menu;
    }

    private MenuResponse toMenuResponse(Menu menu) {
        return MenuResponse.builder()
                .idMenu(menu.getIdMenu())
                .namaMenu(menu.getNamaMenu())
                .deskripsiMenu(menu.getDeskripsiMenu())
                .hargaMenu(menu.getHargaMenu())
                .stok(menu.getStok())
                .fotoPath(menu.getFotoPath())
                .build();
    }

    @Override
    public MenuResponse updateMenu(UUID id, UpdateMenuRequest request) throws Exception {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Menu dengan ID " + id + " tidak ditemukan"));

        if (request.getStok() < 0) {
            throw new IllegalArgumentException("Stok menu tidak boleh negatif");
        }

        if (request.getHargaMenu() < 0) {
            throw new IllegalArgumentException("Harga menu harus lebih dari 0");
        }

        menu.setNamaMenu(request.getNamaMenu());
        menu.setDeskripsiMenu(request.getDeskripsiMenu());
        menu.setHargaMenu(request.getHargaMenu());
        menu.setStok(request.getStok());
        menu.setFotoPath(request.getFotoPath());

        menuRepository.save(menu);

        return toMenuResponse(menu);
    }

    @Override
    public MenuResponse updateStok(UUID id, UpdateStokRequest request) throws Exception {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Menu dengan ID " + id + " tidak ditemukan"));

        if (request.getStok() < 0) {
            throw new IllegalArgumentException("Stok menu tidak boleh negatif");
        }
        menu.setStok(request.getStok());

        menuRepository.save(menu);

        return toMenuResponse(menu);
    }

    @Override
    public void deleteMenu(UUID id) throws Exception {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Menu dengan ID " + id + " tidak ditemukan"));

        menu.setDeleted(true);

        menuRepository.save(menu);
    }

    @Override
    public List<Menu> getAllMenus() {
        return menuRepository.findAll().stream()
                .filter(menu -> !menu.isDeleted())
                .toList();
    }

    @Override
    public Menu getMenuById(UUID id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Menu not found with id: " + id));
        if (menu.isDeleted()) {
            throw new NoSuchElementException("Menu with id: " + id + " is deleted");
        }
        return menu;
    }
}
