package com.joseph.ismes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal todaySales;
    private int todaySalesCount;
    private BigDecimal todayExpenses;
    private BigDecimal estimatedProfit;
    private List<ProductResponse> lowStockProducts;
    private List<TransactionSummary> recentTransactions;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TransactionSummary {
        private Long id;
        private String type; // "SALE" or "EXPENSE"
        private String reference;
        private BigDecimal amount;
        private String timestamp; // ISO-8601
    }
}
