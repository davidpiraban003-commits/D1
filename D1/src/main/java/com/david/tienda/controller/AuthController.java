package com.david.tienda.controller;

import com.david.tienda.dto.HttpGlobalResponseDTO;
import com.david.tienda.dto.LoginRequestDTO;
import com.david.tienda.dto.LoginResponseDTO;
import com.david.tienda.dto.RegisterRequestDTO;
import com.david.tienda.dto.TokenResponseDTO;
import com.david.tienda.dto.UserResponseDTO;
import com.david.tienda.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> register(@RequestBody RegisterRequestDTO body) {
        return respond(HttpStatus.CREATED, "Usuario registrado correctamente", () -> authService.register(body));
    }

    @PostMapping("/login")
    public ResponseEntity<HttpGlobalResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO body) {
        return respond(HttpStatus.OK, "Inicio de sesión exitoso", () -> authService.login(body));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<HttpGlobalResponseDTO<TokenResponseDTO>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return respond(HttpStatus.OK, "Token renovado", () -> authService.refresh(authorization));
    }
}
