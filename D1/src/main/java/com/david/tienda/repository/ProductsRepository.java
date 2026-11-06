package com.david.tienda.repository;

import com.david.tienda.entity.Products;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductsRepository extends JpaRepository<Products, Integer> {

    List<Products> findByCategoryId(Integer categoryId);

    List<Products> findBySellerId(Integer sellerId);

    boolean existsByCategoryId(Integer categoryId);

    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Products p where p.id = :id")
    Optional<Products> findByIdForUpdate(@Param("id") Integer id);
}
