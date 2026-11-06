package com.david.tienda.controller;

import com.david.tienda.dto.CategoryRequestDTO;
import com.david.tienda.dto.CategoryResponseDTO;
import com.david.tienda.dto.HttpGlobalResponseDTO;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.security.RequiresRole;
import com.david.tienda.service.CategoriesService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoriesController extends BaseController {

    private final CategoriesService categoriesService;

    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<List<CategoryResponseDTO>>> list() {
        return respond(HttpStatus.OK, "Lista de categorías", categoriesService::findAll);
    }

    @PostMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<CategoryResponseDTO>> create(@RequestBody CategoryRequestDTO body) {
        return respond(HttpStatus.CREATED, "Categoría creada correctamente", () -> categoriesService.create(body));
    }

    @DeleteMapping("/{id}")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> delete(@PathVariable Integer id) {
        return respond(HttpStatus.OK, "Categoría eliminada", () -> {
            categoriesService.delete(id);
            return null;
        });
    }
}
