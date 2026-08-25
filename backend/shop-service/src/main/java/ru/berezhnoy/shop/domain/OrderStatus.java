package ru.berezhnoy.shop.domain;

public enum OrderStatus {
    NEW,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    PAYMENT_CANCELLED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
