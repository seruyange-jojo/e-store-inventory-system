package com.joseph.ismes.service;

import com.joseph.ismes.dto.DashboardSummaryResponse;
import com.joseph.ismes.dto.ProductResponse;
import com.joseph.ismes.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;

    // TODO: inject SaleRepository and ExpenseRepository once those modules exist,
    // and replace the ZERO placeholders below with real aggregation queries
    // (e.g. SUM(total_amount) WHERE sale_date = CURRENT_DATE). The response shape
    // is already final, so the frontend won't need to change when this is wired up.

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        List<ProductResponse> lowStock = productRepository.findLowStockProducts().stream()
                .map(ProductResponse::fromEntity)
                .toList();

        return DashboardSummaryResponse.builder()
                .todaySales(BigDecimal.ZERO)
                .todaySalesCount(0)
                .todayExpenses(BigDecimal.ZERO)
                .estimatedProfit(BigDecimal.ZERO)
                .lowStockProducts(lowStock)
                .recentTransactions(Collections.emptyList())
                .build();
    }
}
