package ru.berezhnoy.payment;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        Integer orderId,
        BigDecimal amount,
        String paymentMethod
) {
}
