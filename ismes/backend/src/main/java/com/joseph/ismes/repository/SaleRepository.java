package com.joseph.ismes.repository;

import com.joseph.ismes.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findTop50ByOrderBySaleDateDesc();
    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime from, LocalDateTime to);
    long countBySaleDateBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.saleDate >= :from AND s.saleDate < :to")
    BigDecimal sumTotalAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(i.buyingPriceAtSale * i.quantity), 0) FROM SaleItem i WHERE i.sale.saleDate >= :from AND i.sale.saleDate < :to")
    BigDecimal sumCostOfGoodsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}