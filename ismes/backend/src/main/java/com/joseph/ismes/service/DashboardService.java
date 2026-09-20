package com.joseph.ismes.service;

import com.joseph.ismes.dto.DashboardSummaryResponse;
import com.joseph.ismes.dto.ProductResponse;
import com.joseph.ismes.repository.ProductRepository;
import com.joseph.ismes.repository.ExpenseRepository;
import com.joseph.ismes.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        List<ProductResponse> lowStock = productRepository.findLowStockProducts().stream()
                .map(ProductResponse::fromEntity)
                .toList();
        BigDecimal todaySales = saleRepository.sumTotalAmountBetween(startOfDay, startOfTomorrow);
        BigDecimal todayCostOfGoods = saleRepository.sumCostOfGoodsBetween(startOfDay, startOfTomorrow);
        BigDecimal todayExpenses = expenseRepository.sumAmountBetween(today, today);
        List<DashboardSummaryResponse.TransactionSummary> transactions = new ArrayList<>();
        saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfDay, startOfTomorrow).stream()
            .limit(10)
            .forEach(sale -> transactions.add(DashboardSummaryResponse.TransactionSummary.builder()
                .id(sale.getId())
                .type("SALE")
                .reference(sale.getReceiptNumber())
                .amount(sale.getTotalAmount())
                .timestamp(sale.getSaleDate().toString())
                .build()));
        expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(today, today).stream()
            .limit(10)
            .forEach(expense -> transactions.add(DashboardSummaryResponse.TransactionSummary.builder()
                .id(expense.getId())
                .type("EXPENSE")
                .reference(expense.getDescription())
                .amount(expense.getAmount())
                .timestamp(expense.getExpenseDate().atTime(LocalTime.NOON).toString())
                .build()));

        return DashboardSummaryResponse.builder()
            .todaySales(todaySales)
            .todaySalesCount((int) saleRepository.countBySaleDateBetween(startOfDay, startOfTomorrow))
            .todayExpenses(todayExpenses)
                .estimatedProfit(todaySales.subtract(todayCostOfGoods).subtract(todayExpenses))
                .lowStockProducts(lowStock)
            .recentTransactions(transactions.stream().limit(10).toList())
                .build();
    }
}
