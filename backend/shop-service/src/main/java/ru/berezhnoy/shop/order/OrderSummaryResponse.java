package ru.berezhnoy.shop.order;

import ru.berezhnoy.shop.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Integer id,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
