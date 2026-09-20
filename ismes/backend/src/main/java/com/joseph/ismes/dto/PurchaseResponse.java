package com.joseph.ismes.dto;

import com.joseph.ismes.entity.Purchase;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PurchaseResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDate purchaseDate;
    private String referenceNumber;
    private BigDecimal totalAmount;

    public static PurchaseResponse fromEntity(Purchase purchase) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .supplierId(purchase.getSupplier().getId())
                .supplierName(purchase.getSupplier().getName())
                .purchaseDate(purchase.getPurchaseDate())
                .referenceNumber(purchase.getReferenceNumber())
                .totalAmount(purchase.getTotalAmount())
                .build();
    }
}