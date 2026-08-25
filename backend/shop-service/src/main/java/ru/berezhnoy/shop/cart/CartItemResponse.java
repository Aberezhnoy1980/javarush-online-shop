package ru.berezhnoy.shop.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Integer productId,
        String name,
        BigDecimal price,
        Integer quantity,
        Integer stockQuantity,
        BigDecimal lineTotal
) {
}
