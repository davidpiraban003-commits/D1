package com.david.tienda.dto;

/** Creación de usuarios por un ADMIN: aquí sí se puede elegir el rol. */
public record UserCreateRequestDTO(String username, String email, String password, Integer rolId) {
}
