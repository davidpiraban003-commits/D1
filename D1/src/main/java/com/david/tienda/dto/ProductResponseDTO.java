package com.david.tienda.dto;

import java.math.BigDecimal;

public record ProductResponseDTO(Integer id, String name, String description, BigDecimal price, Integer stock,
                                 Integer categoryId, String categoryName, Integer sellerId, String sellerUsername) {
}
