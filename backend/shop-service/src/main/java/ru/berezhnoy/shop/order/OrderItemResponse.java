package ru.berezhnoy.shop.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Integer productId,
        String productName,
        BigDecimal priceAtTime,
        Integer quantity,
        BigDecimal lineTotal
) {
}
