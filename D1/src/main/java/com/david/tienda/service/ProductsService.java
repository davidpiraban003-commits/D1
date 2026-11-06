package com.david.tienda.service;

import com.david.tienda.dto.ProductRequestDTO;
import com.david.tienda.dto.ProductResponseDTO;
import com.david.tienda.entity.Categories;
import com.david.tienda.entity.Products;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.repository.CategoriesRepository;
import com.david.tienda.repository.OrdersRepository;
import com.david.tienda.repository.ProductsRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;
    private final OrdersRepository ordersRepository;
    private final UsersService usersService;

    public ProductsService(ProductsRepository productsRepository, CategoriesRepository categoriesRepository,
                           OrdersRepository ordersRepository, UsersService usersService) {
        this.productsRepository = productsRepository;
        this.categoriesRepository = categoriesRepository;
        this.ordersRepository = ordersRepository;
        this.usersService = usersService;
    }

    public List<ProductResponseDTO> findAll(Integer categoryId) {
        List<Products> products = (categoryId == null)
                ? productsRepository.findAll()
                : productsRepository.findByCategoryId(categoryId);
        return products.stream().map(this::toDto).toList();
    }

    public ProductResponseDTO findById(Integer id) {
        return toDto(getProduct(id));
    }

    public List<ProductResponseDTO> findMine(Integer sellerId) {
        return productsRepository.findBySellerId(sellerId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ProductResponseDTO create(Integer sellerId, ProductRequestDTO dto) {
        validate(dto);
        Products product = new Products();
        product.setSeller(usersService.getUser(sellerId));
        applyData(product, dto);
        return toDto(productsRepository.save(product));
    }

    @Transactional
    public ProductResponseDTO update(Integer id, ProductRequestDTO dto, Integer requesterId, Integer requesterRolId) {
        Products product = getProduct(id);
        checkOwnership(product, requesterId, requesterRolId);
        validate(dto);
        applyData(product, dto);
        return toDto(productsRepository.save(product));
    }

    @Transactional
    public void delete(Integer id, Integer requesterId, Integer requesterRolId) {
        Products product = getProduct(id);
        checkOwnership(product, requesterId, requesterRolId);
        if (ordersRepository.existsByProductId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el producto tiene pedidos asociados");
        }
        productsRepository.delete(product);
    }

    private void checkOwnership(Products product, Integer requesterId, Integer requesterRolId) {
        boolean isAdmin = requesterRolId != null && requesterRolId == RoleEnum.ADMIN.getId();
        if (!isAdmin && !product.getSeller().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes modificar ni eliminar productos de otro vendedor");
        }
    }

    private Products getProduct(Integer id) {
        return productsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private void validate(ProductRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank() || dto.name().trim().length() > 150) {
            throw badRequest("El nombre es obligatorio y puede tener máximo 150 caracteres");
        }
        if (dto.description() != null && dto.description().length() > 500) {
            throw badRequest("La descripción puede tener máximo 500 caracteres");
        }
        if (dto.price() == null || dto.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("El precio es obligatorio y debe ser mayor que cero");
        }
        if (dto.stock() == null || dto.stock() < 0) {
            throw badRequest("El stock es obligatorio y no puede ser negativo");
        }
        if (dto.categoryId() == null) {
            throw badRequest("La categoría es obligatoria (categoryId)");
        }
    }

    private void applyData(Products product, ProductRequestDTO dto) {
        Categories category = categoriesRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        product.setName(dto.name().trim());
        product.setDescription(dto.description());
        product.setPrice(dto.price().setScale(2, RoundingMode.HALF_UP));
        product.setStock(dto.stock());
        product.setCategory(category);
    }

    private ProductResponseDTO toDto(Products product) {
        return new ProductResponseDTO(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStock(), product.getCategory().getId(),
                product.getCategory().getName(), product.getSeller().getId(), product.getSeller().getUsername());
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
