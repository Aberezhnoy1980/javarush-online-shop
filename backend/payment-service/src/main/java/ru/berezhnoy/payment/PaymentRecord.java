package ru.berezhnoy.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

final class PaymentRecord {

    private final String id;
    private final Integer orderId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String transactionId;
    private PaymentRecordStatus status;

    PaymentRecord(String id, Integer orderId, BigDecimal amount, String paymentMethod, String transactionId) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.status = PaymentRecordStatus.COMPLETED;
    }

    String id() {
        return id;
    }

    void refund() {
        if (status != PaymentRecordStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a COMPLETED payment can be cancelled");
        }
        status = PaymentRecordStatus.REFUNDED;
    }

    PaymentResponse toResponse() {
        return new PaymentResponse(id, orderId, amount, status, transactionId, paymentMethod);
    }
}
