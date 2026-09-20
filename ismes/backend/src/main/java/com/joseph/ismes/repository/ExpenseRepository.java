package com.joseph.ismes.repository;

import com.joseph.ismes.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findTop50ByOrderByExpenseDateDescCreatedAtDesc();
    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumAmountBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}