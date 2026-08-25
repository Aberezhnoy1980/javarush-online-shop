package ru.berezhnoy.shop.payment;

import java.math.BigDecimal;

public record PaymentGatewayResponse(
        String id,
        Integer orderId,
        BigDecimal amount,
        String status,
        String transactionId,
        String paymentMethod
) {
}
