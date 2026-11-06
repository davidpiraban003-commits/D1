package com.david.tienda.filter;

import com.david.tienda.security.SecurityResponseWriter;
import com.david.tienda.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;



public class JwtValidationFilter extends OncePerRequestFilter {

    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_ROL_ID = "rolId";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtValidationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isPublic(request.getMethod(), pathWithinContext(request))) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX) || header.substring(BEARER_PREFIX.length()).isBlank()) {
            reject(response, "Token ausente: envía el header Authorization: Bearer <token>");
            return;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();

        try {
            Claims claims = jwtService.parse(token);
            request.setAttribute(ATTR_USERNAME, claims.getSubject());
            request.setAttribute(ATTR_USER_ID, jwtService.getUserId(claims));
            request.setAttribute(ATTR_ROL_ID, jwtService.getRolId(claims));
        } catch (ExpiredJwtException e) {
            reject(response, "El token ha expirado");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            reject(response, "Token inválido");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        SecurityResponseWriter.write(response, objectMapper, HttpStatus.UNAUTHORIZED, message);
    }

    private String pathWithinContext(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

  
    private boolean isPublic(String method, String path) {
        if (path.equals("/auth") || path.startsWith("/auth/")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method)) {
            return path.equals("/products") || path.matches("/products/\\d+");
        }
        return false;
    }
}
