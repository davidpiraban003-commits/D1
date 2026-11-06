package com.david.tienda.security;

import com.david.tienda.filter.JwtValidationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class RoleInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public RoleInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (requiresRole == null) {
            requiresRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        }
        if (requiresRole == null) {
            return true;
        }

        Object rolId = request.getAttribute(JwtValidationFilter.ATTR_ROL_ID);
        if (rolId == null) {
            SecurityResponseWriter.write(response, objectMapper, HttpStatus.UNAUTHORIZED,
                    "Debes autenticarte para usar este recurso");
            return false;
        }

        boolean allowed = Arrays.stream(requiresRole.value())
                .anyMatch(role -> role.getId() == (Integer) rolId);
        if (!allowed) {
            SecurityResponseWriter.write(response, objectMapper, HttpStatus.FORBIDDEN,
                    "Tu rol no tiene permiso para esta acción");
            return false;
        }
        return true;
    }
}
