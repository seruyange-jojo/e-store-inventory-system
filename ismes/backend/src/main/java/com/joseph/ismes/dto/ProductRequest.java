package com.joseph.ismes.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest extends ProductUpdateRequest {

    @NotNull(message = "Opening stock is required")
    @Min(value = 0, message = "Opening stock cannot be negative")
    private Integer openingStock;
}
