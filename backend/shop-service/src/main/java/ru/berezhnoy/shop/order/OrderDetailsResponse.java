package ru.berezhnoy.shop.order;

import ru.berezhnoy.shop.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsResponse(
        Integer id,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        PaymentSummaryResponse payment
) {
}
