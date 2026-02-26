package com.brewforce.pkpl.backend.repository;

import com.brewforce.pkpl.backend.model.Menu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MenuRepositoryTest {

    @Mock
    private MenuRepository menuRepository;

    @Test
    public void testFindById_WithExistingId_ShouldReturnMenu() {
        // Arrange
        UUID menuId = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setIdMenu(menuId);
        menu.setNamaMenu("Nasi Goreng");
        menu.setHargaMenu(20000);
        menu.setStok(10);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        // Act
        Optional<Menu> foundMenu = menuRepository.findById(menuId);

        // Assert
        assertThat(foundMenu).isPresent();
        assertThat(foundMenu.get().getIdMenu()).isEqualTo(menuId);
        assertThat(foundMenu.get().getNamaMenu()).isEqualTo("Nasi Goreng");
        assertThat(foundMenu.get().getHargaMenu()).isEqualTo(20000L);
        assertThat(foundMenu.get().getStok()).isEqualTo(10);
    }

    @Test
    public void testFindById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID menuId = UUID.randomUUID();
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        // Act
        Optional<Menu> foundMenu = menuRepository.findById(menuId);

        // Assert
        assertThat(foundMenu).isNotPresent();
    }
}