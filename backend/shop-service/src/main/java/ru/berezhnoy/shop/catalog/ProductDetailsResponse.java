package ru.berezhnoy.shop.catalog;

import java.math.BigDecimal;

public record ProductDetailsResponse(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        Integer brandId,
        String brandName,
        Integer categoryId,
        String categoryName
) {
}
