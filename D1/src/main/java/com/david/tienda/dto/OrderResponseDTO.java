package com.david.tienda.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponseDTO(Integer id, Integer clientId, String clientUsername, Integer productId,
                               String productName, Integer quantity, BigDecimal total, String status,
                               LocalDateTime createdAt) {
}
