package ru.berezhnoy.shop.analytics;

import java.math.BigDecimal;

public record TopProductResponse(
        Integer productId,
        String productName,
        Long unitsSold,
        BigDecimal revenue,
        Double averageRating
) {
}
