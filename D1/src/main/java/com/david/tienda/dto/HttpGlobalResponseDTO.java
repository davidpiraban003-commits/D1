package com.david.tienda.dto;

/** Forma única de todas las respuestas de la API: un mensaje y, si aplica, los datos. */
public record HttpGlobalResponseDTO<T>(String message, T data) {
}
