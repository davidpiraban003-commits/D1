package com.david.tienda.service;

import com.david.tienda.dto.CategoryRequestDTO;
import com.david.tienda.dto.CategoryResponseDTO;
import com.david.tienda.entity.Categories;
import com.david.tienda.repository.CategoriesRepository;
import com.david.tienda.repository.ProductsRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final ProductsRepository productsRepository;

    public CategoriesService(CategoriesRepository categoriesRepository, ProductsRepository productsRepository) {
        this.categoriesRepository = categoriesRepository;
        this.productsRepository = productsRepository;
    }

    public List<CategoryResponseDTO> findAll() {
        return categoriesRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank() || dto.name().trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio y puede tener máximo 100 caracteres");
        }
        String name = dto.name().trim();
        if (categoriesRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        Categories category = new Categories();
        category.setName(name);
        return toDto(categoriesRepository.save(category));
    }

    
    @Transactional
    public void delete(Integer id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        if (productsRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: la categoría tiene productos");
        }
        categoriesRepository.delete(category);
    }

    private CategoryResponseDTO toDto(Categories category) {
        return new CategoryResponseDTO(category.getId(), category.getName());
    }
}
