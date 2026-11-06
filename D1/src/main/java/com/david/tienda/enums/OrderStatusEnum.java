package com.david.tienda.enums;

import java.util.Arrays;
import java.util.Optional;

public enum OrderStatusEnum {
    PENDIENTE,
    ENVIADO,
    ENTREGADO,
    CANCELADO;

    public static Optional<OrderStatusEnum> fromText(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String normalized = text.trim();
        return Arrays.stream(values()).filter(s -> s.name().equalsIgnoreCase(normalized)).findFirst();
    }
}
