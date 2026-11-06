package com.david.tienda.repository;

import com.david.tienda.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
