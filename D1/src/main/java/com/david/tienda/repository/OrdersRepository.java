package com.david.tienda.repository;

import com.david.tienda.entity.Orders;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    List<Orders> findByClientIdOrderByCreatedAtDesc(Integer clientId);

    List<Orders> findByProductSellerIdOrderByCreatedAtDesc(Integer sellerId);

    List<Orders> findAllByOrderByCreatedAtDesc();

    boolean existsByProductId(Integer productId);
}
