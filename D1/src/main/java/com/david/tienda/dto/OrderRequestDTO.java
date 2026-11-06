package com.david.tienda.dto;

/** No trae clientId ni total: el cliente sale del token y el total lo calcula el servidor. */
public record OrderRequestDTO(Integer productId, Integer quantity) {
}
