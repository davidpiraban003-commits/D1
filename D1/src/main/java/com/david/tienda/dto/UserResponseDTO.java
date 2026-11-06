package com.david.tienda.dto;

/** Datos públicos de un usuario. Por diseño no tiene campo de contraseña. */
public record UserResponseDTO(Integer id, String username, String email, boolean active, Integer rolId, String rol) {
}
