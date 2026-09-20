package com.joseph.ismes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaleRequest {
    private String customerName;
    private String customerPhone;
    @NotNull
    private String paymentMethod;
    @Valid
    @NotEmpty
    private List<SaleItemRequest> items;

    @Getter
    @Setter
    public static class SaleItemRequest {
        @NotNull
        private Long productId;
        @NotNull
        private Integer quantity;
        @NotNull
        private java.math.BigDecimal unitPrice;
    }
}