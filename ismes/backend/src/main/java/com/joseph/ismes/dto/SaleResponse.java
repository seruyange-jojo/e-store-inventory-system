package com.joseph.ismes.dto;

import com.joseph.ismes.entity.Sale;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SaleResponse {
    private Long id;
    private String receiptNumber;
    private LocalDateTime saleDate;
    private String customerName;
    private String paymentMethod;
    private BigDecimal totalAmount;

    public static SaleResponse fromEntity(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .receiptNumber(sale.getReceiptNumber())
                .saleDate(sale.getSaleDate())
                .customerName(sale.getCustomerName())
                .paymentMethod(sale.getPaymentMethod())
                .totalAmount(sale.getTotalAmount())
                .build();
    }
}