package ru.berezhnoy.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentProcessor {

    private final Map<String, PaymentRecord> payments = new ConcurrentHashMap<>();
    private final double failureRate;

    public PaymentProcessor(@Value("${payment.failure-rate:0}") double failureRate) {
        if (failureRate < 0 || failureRate > 1) {
            throw new IllegalArgumentException("payment.failure-rate must be between 0 and 1");
        }
        this.failureRate = failureRate;
    }

    public PaymentResponse charge(CreatePaymentRequest request) {
        maybeFail();
        if (request == null || request.orderId() == null || request.amount() == null || request.paymentMethod() == null
                || request.paymentMethod().isBlank() || request.amount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderId, positive amount and paymentMethod are required");
        }
        String id = UUID.randomUUID().toString();
        String transactionId = "txn_" + id.replace("-", "").substring(0, 12);
        PaymentRecord record = new PaymentRecord(id, request.orderId(), request.amount(), request.paymentMethod().trim(), transactionId);
        payments.put(id, record);
        return record.toResponse();
    }

    public PaymentResponse get(String paymentId) {
        maybeFail();
        return require(paymentId).toResponse();
    }

    public PaymentResponse cancel(String paymentId) {
        maybeFail();
        PaymentRecord record = require(paymentId);
        record.refund();
        return record.toResponse();
    }

    private PaymentRecord require(String paymentId) {
        PaymentRecord record = payments.get(paymentId);
        if (record == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment " + paymentId + " not found");
        }
        return record;
    }

    private void maybeFail() {
        if (failureRate > 0 && ThreadLocalRandom.current().nextDouble() < failureRate) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment provider temporarily unavailable");
        }
    }
}
