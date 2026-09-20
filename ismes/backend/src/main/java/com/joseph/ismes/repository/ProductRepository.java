package com.joseph.ismes.repository;

import com.joseph.ismes.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    List<Product> findByActiveTrue();

    List<Product> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndProductCodeContainingIgnoreCase(
            String name, String code);

    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.minStockLevel AND p.active = true")
    List<Product> findLowStockProducts();
}
