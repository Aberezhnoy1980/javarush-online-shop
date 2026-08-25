package ru.berezhnoy.shop.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;

    public OrderController(OrderService orderService, OrderPaymentService orderPaymentService) {
        this.orderService = orderService;
        this.orderPaymentService = orderPaymentService;
    }

    @PostMapping
    public ResponseEntity<OrderDetailsResponse> checkout() {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout());
    }

    @GetMapping
    public List<OrderSummaryResponse> listOrders() {
        return orderService.listOrders();
    }

    @GetMapping("/{orderId}")
    public OrderDetailsResponse getOrder(@PathVariable("orderId") Integer orderId) {
        return orderService.getOrder(orderId);
    }

    @PostMapping("/{orderId}/payment")
    public OrderDetailsResponse pay(@PathVariable("orderId") Integer orderId) {
        return orderPaymentService.pay(orderId);
    }

    @PostMapping("/{orderId}/payment/cancel")
    public OrderDetailsResponse cancelPayment(@PathVariable("orderId") Integer orderId) {
        return orderPaymentService.cancel(orderId);
    }
}
