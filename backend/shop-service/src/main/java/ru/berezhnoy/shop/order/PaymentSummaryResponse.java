package ru.berezhnoy.shop.order;

import ru.berezhnoy.shop.domain.PaymentStatus;

import java.math.BigDecimal;

public record PaymentSummaryResponse(
        Integer id,
        BigDecimal amount,
        PaymentStatus status,
        String paymentMethod
) {
}
