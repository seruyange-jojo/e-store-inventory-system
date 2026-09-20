package com.joseph.ismes.dto;

import com.joseph.ismes.entity.Sale;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SaleDetailResponse {
    private Long id;
    private String receiptNumber;
    private LocalDateTime saleDate;
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private boolean belowCost;
    }

    public static SaleDetailResponse fromEntity(Sale sale) {
        return SaleDetailResponse.builder()
                .id(sale.getId())
                .receiptNumber(sale.getReceiptNumber())
                .saleDate(sale.getSaleDate())
                .customerName(sale.getCustomerName())
                .customerPhone(sale.getCustomerPhone())
                .paymentMethod(sale.getPaymentMethod())
                .totalAmount(sale.getTotalAmount())
                .items(sale.getItems().stream().map(item -> Item.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .belowCost(item.isBelowCost())
                        .build()).toList())
                .build();
    }
}