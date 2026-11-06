package com.david.tienda.service;

import com.david.tienda.entity.Roles;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.repository.RolesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RolesService {

    private final RolesRepository rolesRepository;

    public RolesService(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    public Roles getRole(RoleEnum role) {
        return rolesRepository.findById(role.getId()).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "El rol " + role + " no existe en la tabla roles; ejecuta tienda.sql"));
    }
}
