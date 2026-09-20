package com.joseph.ismes.dto;

import com.joseph.ismes.entity.Purchase;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PurchaseResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDate purchaseDate;
    private String referenceNumber;
    private BigDecimal totalAmount;
    private List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitCost;
        private BigDecimal subtotal;
    }

    public static PurchaseResponse fromEntity(Purchase purchase) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .supplierId(purchase.getSupplier().getId())
                .supplierName(purchase.getSupplier().getName())
                .purchaseDate(purchase.getPurchaseDate())
                .referenceNumber(purchase.getReferenceNumber())
                .totalAmount(purchase.getTotalAmount())
                .items(purchase.getItems().stream().map(item -> Item.builder()
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .quantity(item.getQuantity())
                    .unitCost(item.getUnitCost())
                    .subtotal(item.getSubtotal())
                    .build()).toList())
                .build();
    }
}