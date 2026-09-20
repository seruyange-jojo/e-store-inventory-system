package com.joseph.ismes.dto;

import com.joseph.ismes.entity.Expense;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ExpenseResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;

    public static ExpenseResponse fromEntity(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .categoryId(expense.getCategory() == null ? null : expense.getCategory().getId())
                .categoryName(expense.getCategory() == null ? null : expense.getCategory().getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .build();
    }
}