package ru.berezhnoy.shop.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berezhnoy.shop.cart.CartRepository;
import ru.berezhnoy.shop.cart.CartService;
import ru.berezhnoy.shop.catalog.ProductRepository;
import ru.berezhnoy.shop.domain.Cart;
import ru.berezhnoy.shop.domain.CartItem;
import ru.berezhnoy.shop.domain.Order;
import ru.berezhnoy.shop.domain.OrderItem;
import ru.berezhnoy.shop.domain.Payment;
import ru.berezhnoy.shop.domain.Product;
import ru.berezhnoy.shop.user.DemoUserService;
import ru.berezhnoy.shop.web.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final DemoUserService demoUserService;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(
            DemoUserService demoUserService,
            CartService cartService,
            CartRepository cartRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository
    ) {
        this.demoUserService = demoUserService;
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDetailsResponse checkout() {
        Cart cart = cartService.requireCart();
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        List<Integer> productIds = cart.getItems().stream()
                .map(item -> item.getProduct().getId())
                .toList();
        Map<Integer, Product> lockedProducts = productRepository.findAllByIdForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderItem> snapshots = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = lockedProducts.get(cartItem.getProduct().getId());
            if (product == null) {
                throw new ResourceNotFoundException("Product " + cartItem.getProduct().getId() + " not found");
            }
            product.decreaseStock(cartItem.getQuantity());
            OrderItem orderItem = OrderItem.snapshot(product, cartItem.getQuantity());
            snapshots.add(orderItem);
            total = total.add(orderItem.getLineTotal());
        }

        Order order = Order.place(demoUserService.requireUser(), total);
        snapshots.forEach(order::addItem);
        order.attachPayment(Payment.pending(order, total));
        Order saved = orderRepository.save(order);
        cart.clearItems();
        cartRepository.save(cart);
        return toDetails(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> listOrders() {
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(demoUserService.userId()).stream()
                .map(order -> new OrderSummaryResponse(
                        order.getId(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrder(Integer orderId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, demoUserService.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " not found"));
        return toDetails(order);
    }

    private static OrderDetailsResponse toDetails(Order order) {
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
                payment.getPaymentMethod()
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
