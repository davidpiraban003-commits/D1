package com.david.tienda.controller;

import com.david.tienda.dto.HttpGlobalResponseDTO;
import com.david.tienda.filter.JwtValidationFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * Ayudantes comunes de los controllers:
 *  - respond(): ejecuta la acción y arma la respuesta HttpGlobalResponseDTO; si el servicio lanza
 *    ResponseStatusException (400, 403, 404, 409...) la convierte en la respuesta con ese código.
 *  - userId() / rolId(): leen los atributos que dejó el JwtValidationFilter.
 */
public abstract class BaseController {

    protected <T> ResponseEntity<HttpGlobalResponseDTO<T>> respond(HttpStatus successStatus, String successMessage,
                                                                   Supplier<T> action) {
        try {
            T data = action.get();
            return ResponseEntity.status(successStatus).body(new HttpGlobalResponseDTO<T>(successMessage, data));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpGlobalResponseDTO<T>(e.getReason(), null));
        }
    }

    protected Integer userId(HttpServletRequest request) {
        return (Integer) request.getAttribute(JwtValidationFilter.ATTR_USER_ID);
    }

    protected Integer rolId(HttpServletRequest request) {
        return (Integer) request.getAttribute(JwtValidationFilter.ATTR_ROL_ID);
    }
}
