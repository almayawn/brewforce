package com.brewforce.pkpl.backend.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuResponse {
    private UUID idMenu;
    private String namaMenu;
    private String deskripsiMenu;
    private int hargaMenu;
    private int stok;
    private String fotoPath;
}
