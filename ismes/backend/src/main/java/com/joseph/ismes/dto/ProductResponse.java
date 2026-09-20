package com.joseph.ismes.dto;

import com.joseph.ismes.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String unit;
    private BigDecimal buyingPrice;
    private BigDecimal sellingPrice;
    private Integer openingStock;
    private Integer currentStock;
    private Integer minStockLevel;
    private boolean lowStock;
    private boolean active;

    public static ProductResponse fromEntity(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .productCode(p.getProductCode())
                .name(p.getName())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .description(p.getDescription())
                .unit(p.getUnit())
                .buyingPrice(p.getBuyingPrice())
                .sellingPrice(p.getSellingPrice())
                .openingStock(p.getOpeningStock())
                .currentStock(p.getCurrentStock())
                .minStockLevel(p.getMinStockLevel())
                .lowStock(p.isLowStock())
                .active(p.isActive())
                .build();
    }
}
