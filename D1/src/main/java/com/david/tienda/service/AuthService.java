package com.david.tienda.service;

import com.david.tienda.dto.LoginRequestDTO;
import com.david.tienda.dto.LoginResponseDTO;
import com.david.tienda.dto.RegisterRequestDTO;
import com.david.tienda.dto.TokenResponseDTO;
import com.david.tienda.dto.UserResponseDTO;
import com.david.tienda.entity.Users;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UsersRepository usersRepository;
    private final UsersService usersService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsersRepository usersRepository, UsersService usersService, JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.usersService = usersService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

   
    public UserResponseDTO register(RegisterRequestDTO dto) {
        Users user = usersService.createUser(dto.username(), dto.email(), dto.password(), RoleEnum.CLIENTE);
        return usersService.toDto(user);
    }

    
    public LoginResponseDTO login(LoginRequestDTO dto) {
        if (dto.user() == null || dto.user().isBlank() || dto.password() == null || dto.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user y password son obligatorios");
        }
        String identifier = dto.user().trim();

        Users user = usersRepository.findByUsername(identifier)
                .or(() -> usersRepository.findByEmail(identifier))
                .orElseThrow(() -> unauthorized("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw unauthorized("Credenciales inválidas");
        }
        if (!user.isActive()) {
            throw unauthorized("Usuario inactivo");
        }
        return new LoginResponseDTO(jwtService.generate(user), usersService.toDto(user));
    }

   
    public TokenResponseDTO refresh(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)
                || authorizationHeader.substring(BEARER_PREFIX.length()).isBlank()) {
            throw unauthorized("Token ausente: envía el header Authorization: Bearer <token>");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        Claims claims;
        try {
            claims = jwtService.parse(token);
        } catch (ExpiredJwtException e) {
            throw unauthorized("El token ha expirado");
        } catch (JwtException | IllegalArgumentException e) {
            throw unauthorized("Token inválido");
        }

        Integer userId;
        try {
            userId = jwtService.getUserId(claims);
        } catch (JwtException e) {
            throw unauthorized("Token inválido");
        }
        Users user = usersRepository.findById(userId)
                .filter(Users::isActive)
                .orElseThrow(() -> unauthorized("El usuario del token no existe o está inactivo"));
        return new TokenResponseDTO(jwtService.generate(user));
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
