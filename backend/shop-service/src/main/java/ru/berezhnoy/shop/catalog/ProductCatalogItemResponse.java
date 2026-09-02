package ru.berezhnoy.shop.catalog;

import java.math.BigDecimal;

public record ProductCatalogItemResponse(
        Integer id,
        String name,
        String shortDescription,
        BigDecimal price,
        Integer stockQuantity,
        String brandName,
        String categoryName
) {
}
