package ru.berezhnoy.shop.payment;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentGatewayResponse charge(Integer orderId, BigDecimal amount, String paymentMethod);

    PaymentGatewayResponse cancel(String paymentId);
}
