package com.david.tienda.controller;

import com.david.tienda.dto.ChangeRoleRequestDTO;
import com.david.tienda.dto.ChangeStatusRequestDTO;
import com.david.tienda.dto.HttpGlobalResponseDTO;
import com.david.tienda.dto.UserCreateRequestDTO;
import com.david.tienda.dto.UserResponseDTO;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.security.RequiresRole;
import com.david.tienda.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController extends BaseController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/me")
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> me(HttpServletRequest request) {
        return respond(HttpStatus.OK, "Perfil del usuario", () -> usersService.getProfile(userId(request)));
    }

    @GetMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<List<UserResponseDTO>>> list() {
        return respond(HttpStatus.OK, "Lista de usuarios", usersService::findAll);
    }

    @PostMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> create(@RequestBody UserCreateRequestDTO body) {
        return respond(HttpStatus.CREATED, "Usuario creado correctamente", () -> usersService.createByAdmin(body));
    }

    @PatchMapping("/{id}/role")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> changeRole(@PathVariable Integer id,
                                                                            @RequestBody ChangeRoleRequestDTO body) {
        return respond(HttpStatus.OK, "Rol actualizado", () -> usersService.changeRole(id, body));
    }

    @PatchMapping("/{id}/status")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> changeStatus(@PathVariable Integer id,
                                                                              @RequestBody ChangeStatusRequestDTO body) {
        return respond(HttpStatus.OK, "Estado actualizado", () -> usersService.changeStatus(id, body));
    }
}
