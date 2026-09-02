package ru.berezhnoy.shop.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.berezhnoy.shop.domain.Order;
import ru.berezhnoy.shop.domain.Payment;
import ru.berezhnoy.shop.payment.PaymentGateway;
import ru.berezhnoy.shop.payment.PaymentGatewayResponse;
import ru.berezhnoy.shop.payment.PaymentUnavailableException;
import ru.berezhnoy.shop.web.ConflictException;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class OrderPaymentService {

    private final OrderService orderService;
    private final PaymentGateway paymentGateway;
    private final TransactionTemplate transactionTemplate;

    public OrderPaymentService(
            OrderService orderService,
            PaymentGateway paymentGateway,
            PlatformTransactionManager transactionManager
    ) {
        this.orderService = orderService;
        this.paymentGateway = paymentGateway;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public OrderDetailsResponse pay(Integer orderId) {
        PaymentCharge charge = Objects.requireNonNull(transactionTemplate.execute(status -> prepareCharge(orderId)));
        try {
            PaymentGatewayResponse gatewayResponse = paymentGateway.charge(
                    charge.orderId(),
                    charge.amount(),
                    charge.paymentMethod()
            );
            return Objects.requireNonNull(
                    transactionTemplate.execute(status -> completePayment(orderId, gatewayResponse.id()))
            );
        } catch (PaymentUnavailableException exception) {
            transactionTemplate.executeWithoutResult(status -> failPayment(orderId));
            throw exception;
        }
    }

    public OrderDetailsResponse cancel(Integer orderId) {
        String gatewayPaymentId = Objects.requireNonNull(transactionTemplate.execute(status -> prepareCancel(orderId)));
        try {
            paymentGateway.cancel(gatewayPaymentId);
            return Objects.requireNonNull(transactionTemplate.execute(status -> completeCancel(orderId)));
        } catch (PaymentUnavailableException exception) {
            throw exception;
        }
    }

    private PaymentCharge prepareCharge(Integer orderId) {
        Order order = orderService.requireOwned(orderId);
        if (!order.allowsPayment()) {
            throw new ConflictException("Order " + orderId + " cannot be paid in status " + order.getStatus());
        }
        Payment payment = requirePayment(order);
        order.markPaymentPending();
        return new PaymentCharge(order.getId(), payment.getAmount(), payment.getPaymentMethod());
    }

    private OrderDetailsResponse completePayment(Integer orderId, String gatewayPaymentId) {
        Order order = orderService.requireOwned(orderId);
        Payment payment = requirePayment(order);
        payment.complete(gatewayPaymentId);
        order.markPaid();
        return OrderMapper.toDetails(order);
    }

    private void failPayment(Integer orderId) {
        Order order = orderService.requireOwned(orderId);
        requirePayment(order).fail();
        order.markPaymentFailed();
    }

    private String prepareCancel(Integer orderId) {
        Order order = orderService.requireOwned(orderId);
        if (!order.allowsCancel()) {
            throw new ConflictException("Order " + orderId + " cannot be cancelled in status " + order.getStatus());
        }
        Payment payment = requirePayment(order);
        if (payment.getTransactionId() == null || payment.getTransactionId().isBlank()) {
            throw new ConflictException("Order " + orderId + " has no completed payment to cancel");
        }
        return payment.getTransactionId();
    }

    private OrderDetailsResponse completeCancel(Integer orderId) {
        Order order = orderService.requireOwned(orderId);
        requirePayment(order).refund();
        order.markPaymentCancelled();
        return OrderMapper.toDetails(order);
    }

    private static Payment requirePayment(Order order) {
        Payment payment = order.getPayment();
        if (payment == null) {
            throw new ConflictException("Order " + order.getId() + " has no payment");
        }
        return payment;
    }

    private record PaymentCharge(Integer orderId, BigDecimal amount, String paymentMethod) {
    }
}
