package com.david.tienda.enums;

import java.util.Arrays;
import java.util.Optional;

public enum RoleEnum {
    ADMIN(1),
    CLIENTE(2),
    VENDEDOR(3);

    private final int id;

    RoleEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /** Busca el rol por su id; vacío si el id no existe. */
    public static Optional<RoleEnum> fromId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(role -> role.id == id).findFirst();
    }
}
