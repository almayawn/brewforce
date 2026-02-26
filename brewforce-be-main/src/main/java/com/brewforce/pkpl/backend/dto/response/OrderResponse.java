package com.brewforce.pkpl.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private UUID idOrder;
    private String status;
    private long totalHarga;
    private String username;
    private List<OrderItemResponse> items;
    private LocalDateTime createdDateTime;
}