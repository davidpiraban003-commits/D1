package com.david.tienda.service;

import com.david.tienda.dto.ChangeRoleRequestDTO;
import com.david.tienda.dto.ChangeStatusRequestDTO;
import com.david.tienda.dto.UserCreateRequestDTO;
import com.david.tienda.dto.UserResponseDTO;
import com.david.tienda.entity.Users;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.repository.UsersRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsersService {

    private static final int MAX_PASSWORD_BYTES = 72;

    private final UsersRepository usersRepository;
    private final RolesService rolesService;
    private final PasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository, RolesService rolesService, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.rolesService = rolesService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO toDto(Users user) {
        return new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.isActive(),
                user.getRol().getId(), user.getRol().getName());
    }

    public Users getUser(Integer id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public UserResponseDTO getProfile(Integer userId) {
        return toDto(getUser(userId));
    }

    public List<UserResponseDTO> findAll() {
        return usersRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public Users createUser(String username, String email, String password, RoleEnum role) {
        if (username == null || username.isBlank() || username.trim().length() > 50) {
            throw badRequest("El username es obligatorio y puede tener máximo 50 caracteres");
        }
        if (email == null || email.isBlank() || !email.contains("@") || email.trim().length() > 255) {
            throw badRequest("El email es obligatorio y debe ser válido");
        }
        if (password == null || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw badRequest("La contraseña es obligatoria y puede tener máximo 72 caracteres");
        }

        String cleanUsername = username.trim();
        String cleanEmail = email.trim();
        if (usersRepository.existsByUsername(cleanUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese username");
        }
        if (usersRepository.existsByEmail(cleanEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }

        Users user = new Users();
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(password)); // se guarda el hash BCrypt
        user.setActive(true);
        user.setRol(rolesService.getRole(role));
        return usersRepository.save(user);
    }

    @Transactional
    public UserResponseDTO createByAdmin(UserCreateRequestDTO dto) {
        RoleEnum role = RoleEnum.fromId(dto.rolId())
                .orElseThrow(() -> badRequest("rolId inválido: usa 1 (ADMIN), 2 (CLIENTE) o 3 (VENDEDOR)"));
        return toDto(createUser(dto.username(), dto.email(), dto.password(), role));
    }

    @Transactional
    public UserResponseDTO changeRole(Integer id, ChangeRoleRequestDTO dto) {
        RoleEnum role = RoleEnum.fromId(dto.rolId())
                .orElseThrow(() -> badRequest("rolId inválido: usa 1 (ADMIN), 2 (CLIENTE) o 3 (VENDEDOR)"));
        Users user = getUser(id);
        user.setRol(rolesService.getRole(role));
        return toDto(usersRepository.save(user));
    }

    @Transactional
    public UserResponseDTO changeStatus(Integer id, ChangeStatusRequestDTO dto) {
        if (dto.active() == null) {
            throw badRequest("El campo active es obligatorio (true o false)");
        }
        Users user = getUser(id);
        user.setActive(dto.active());
        return toDto(usersRepository.save(user));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
