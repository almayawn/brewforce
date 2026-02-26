package com.brewforce.pkpl.backend.service;

import java.util.UUID;

import com.brewforce.pkpl.backend.dto.request.UpdateMenuRequest;
import com.brewforce.pkpl.backend.dto.request.UpdateStokRequest;
import com.brewforce.pkpl.backend.dto.response.MenuResponse;

import java.util.List;

import org.springframework.stereotype.Service;
import com.brewforce.pkpl.backend.model.Menu;

@Service
public interface MenuService {
    Menu createMenu(Menu menu);    
    MenuResponse updateMenu(UUID id, UpdateMenuRequest request) throws Exception;
    MenuResponse updateStok(UUID id, UpdateStokRequest request) throws Exception;
    void deleteMenu(UUID id) throws Exception;
    List<Menu> getAllMenus();
    Menu getMenuById(UUID id);
}
