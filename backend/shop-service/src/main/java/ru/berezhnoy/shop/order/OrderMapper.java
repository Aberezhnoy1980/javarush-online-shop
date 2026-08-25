package ru.berezhnoy.shop.order;

import ru.berezhnoy.shop.domain.Order;
import ru.berezhnoy.shop.domain.Payment;

import java.util.List;

final class OrderMapper {

    private OrderMapper() {
    }

    static OrderDetailsResponse toDetails(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProductNameSnapshot(),
                        item.getPriceAtTime(),
                        item.getQuantity(),
                        item.getLineTotal()
                ))
                .toList();
        Payment payment = order.getPayment();
        PaymentSummaryResponse paymentResponse = payment == null ? null : new PaymentSummaryResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId()
        );
        return new OrderDetailsResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items,
                paymentResponse
        );
    }
}
