package com.david.tienda.dto;

public record LoginResponseDTO(String jwt, UserResponseDTO user) {
}
