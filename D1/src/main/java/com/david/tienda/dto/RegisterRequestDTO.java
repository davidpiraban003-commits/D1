package com.david.tienda.dto;

/** Registro público. No trae rolId: quien se registra siempre queda como CLIENTE. */
public record RegisterRequestDTO(String username, String email, String password) {
}
