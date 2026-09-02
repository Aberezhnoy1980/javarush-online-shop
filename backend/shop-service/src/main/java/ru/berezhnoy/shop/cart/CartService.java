package ru.berezhnoy.shop.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berezhnoy.shop.catalog.ProductRepository;
import ru.berezhnoy.shop.domain.Cart;
import ru.berezhnoy.shop.domain.CartItem;
import ru.berezhnoy.shop.domain.Product;
import ru.berezhnoy.shop.user.DemoUserService;
import ru.berezhnoy.shop.web.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final DemoUserService demoUserService;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(
            DemoUserService demoUserService,
            CartRepository cartRepository,
            ProductRepository productRepository
    ) {
        this.demoUserService = demoUserService;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        return toResponse(requireCart());
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        int quantity = requirePositiveQuantity(request == null ? null : request.quantity());
        Product product = requireProduct(request == null ? null : request.productId());
        Cart cart = requireCart();
        CartItem existing = findItem(cart, product.getId()).orElse(null);
        int nextQuantity = existing == null ? quantity : existing.getQuantity() + quantity;
        ensureStock(product, nextQuantity);
        if (existing == null) {
            cart.addItem(CartItem.of(cart, product, nextQuantity));
        } else {
            existing.changeQuantity(nextQuantity);
            cart.touch();
        }
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(Integer productId, UpdateCartItemRequest request) {
        int quantity = requirePositiveQuantity(request == null ? null : request.quantity());
        Cart cart = requireCart();
        CartItem item = findItem(cart, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item for product " + productId + " not found"));
        ensureStock(item.getProduct(), quantity);
        item.changeQuantity(quantity);
        cart.touch();
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Integer productId) {
        Cart cart = requireCart();
        CartItem item = findItem(cart, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item for product " + productId + " not found"));
        cart.getItems().remove(item);
        cart.touch();
        return toResponse(cart);
    }

    public Cart requireCart() {
        return cartRepository.findByUser_Id(demoUserService.userId())
                .orElseGet(() -> cartRepository.save(Cart.forUser(demoUserService.requireUser())));
    }

    private Product requireProduct(Integer productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not found"));
    }

    private static int requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        return quantity;
    }

    private static void ensureStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough stock for product " + product.getId()
                            + ": requested " + quantity + ", available " + product.getStockQuantity()
            );
        }
    }

    private static Optional<CartItem> findItem(Cart cart, Integer productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

    private static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(CartService::toItem)
                .toList();
        int totalQuantity = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, totalQuantity, totalAmount);
    }

    private static CartItemResponse toItem(CartItem item) {
        Product product = item.getProduct();
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                product.getStockQuantity(),
                lineTotal
        );
    }
}
