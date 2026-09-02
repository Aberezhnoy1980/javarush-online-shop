package ru.berezhnoy.payment;

import java.math.BigDecimal;

public record PaymentResponse(
        String id,
        Integer orderId,
        BigDecimal amount,
        PaymentRecordStatus status,
        String transactionId,
        String paymentMethod
) {
}
