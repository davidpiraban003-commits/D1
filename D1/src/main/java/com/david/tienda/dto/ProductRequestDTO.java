package com.david.tienda.dto;

import java.math.BigDecimal;

/** No trae sellerId: el dueño del producto se toma del token. */
public record ProductRequestDTO(String name, String description, BigDecimal price, Integer stock, Integer categoryId) {
}
