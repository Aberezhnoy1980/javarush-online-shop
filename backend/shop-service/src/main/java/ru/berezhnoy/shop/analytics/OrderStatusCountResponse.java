package ru.berezhnoy.shop.analytics;

import ru.berezhnoy.shop.domain.OrderStatus;

public record OrderStatusCountResponse(
        OrderStatus status,
        Long orderCount
) {
}
