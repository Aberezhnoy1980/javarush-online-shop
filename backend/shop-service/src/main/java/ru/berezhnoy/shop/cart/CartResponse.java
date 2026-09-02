package ru.berezhnoy.shop.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Integer id,
        List<CartItemResponse> items,
        int totalQuantity,
        BigDecimal totalAmount
) {
}
