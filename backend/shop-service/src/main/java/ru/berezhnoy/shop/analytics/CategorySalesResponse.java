package ru.berezhnoy.shop.analytics;

import java.math.BigDecimal;

public record CategorySalesResponse(
        Integer categoryId,
        String categoryName,
        Long orderCount,
        Long unitsSold,
        BigDecimal revenue
) {
}
