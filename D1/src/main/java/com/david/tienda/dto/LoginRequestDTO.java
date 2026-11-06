package com.david.tienda.dto;

/** "user" puede ser el username o el email. */
public record LoginRequestDTO(String user, String password) {
}
