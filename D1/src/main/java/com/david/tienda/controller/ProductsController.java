package com.david.tienda.controller;

import com.david.tienda.dto.HttpGlobalResponseDTO;
import com.david.tienda.dto.ProductRequestDTO;
import com.david.tienda.dto.ProductResponseDTO;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.security.RequiresRole;
import com.david.tienda.service.ProductsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductsController extends BaseController {

    private final ProductsService productsService;

    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public ResponseEntity<HttpGlobalResponseDTO<List<ProductResponseDTO>>> list(
            @RequestParam(required = false) Integer categoryId) {
        return respond(HttpStatus.OK, "Catálogo de productos", () -> productsService.findAll(categoryId));
    }

    @GetMapping("/mine")
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<List<ProductResponseDTO>>> mine(HttpServletRequest request) {
        return respond(HttpStatus.OK, "Mis productos", () -> productsService.findMine(userId(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> detail(@PathVariable Integer id) {
        return respond(HttpStatus.OK, "Detalle del producto", () -> productsService.findById(id));
    }

    @PostMapping
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> create(@RequestBody ProductRequestDTO body,
                                                                           HttpServletRequest request) {
        return respond(HttpStatus.CREATED, "Producto creado correctamente",
                () -> productsService.create(userId(request), body));
    }

    @PutMapping("/{id}")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> update(@PathVariable Integer id,
                                                                           @RequestBody ProductRequestDTO body,
                                                                           HttpServletRequest request) {
        return respond(HttpStatus.OK, "Producto actualizado",
                () -> productsService.update(id, body, userId(request), rolId(request)));
    }

    @DeleteMapping("/{id}")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<Void>> delete(@PathVariable Integer id, HttpServletRequest request) {
        return respond(HttpStatus.OK, "Producto eliminado", () -> {
            productsService.delete(id, userId(request), rolId(request));
            return null;
        });
    }
}
