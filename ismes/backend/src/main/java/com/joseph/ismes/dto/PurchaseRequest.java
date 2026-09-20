package com.joseph.ismes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PurchaseRequest {
    @NotNull
    private Long supplierId;
    @NotNull
    private LocalDate purchaseDate;
    private String referenceNumber;
    private String notes;
    @Valid
    @NotEmpty
    private List<PurchaseItemRequest> items;

    @Getter
    @Setter
    public static class PurchaseItemRequest {
        @NotNull
        private Long productId;
        @NotNull
        private Integer quantity;
        @NotNull
        private BigDecimal unitCost;
    }
}