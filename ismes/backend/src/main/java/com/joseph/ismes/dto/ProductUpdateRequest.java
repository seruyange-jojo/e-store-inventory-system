package com.joseph.ismes.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Editable product details; stock quantities are set only when creating a product. */
@Getter
@Setter
public class ProductUpdateRequest {

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code cannot exceed 50 characters")
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name cannot exceed 150 characters")
    private String name;

    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    private String description;

    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit = "pcs";

    @NotNull(message = "Buying price is required")
    @DecimalMin(value = "0.0", message = "Buying price cannot be negative")
    @Digits(integer = 12, fraction = 2, message = "Buying price allows up to 12 integer digits and 2 decimal places")
    private BigDecimal buyingPrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    @Digits(integer = 12, fraction = 2, message = "Selling price allows up to 12 integer digits and 2 decimal places")
    private BigDecimal sellingPrice;

    @NotNull(message = "Minimum stock level is required")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel;
}
