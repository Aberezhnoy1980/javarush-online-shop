package ru.berezhnoy.shop.cart;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart();
    }

    @PostMapping("/items")
    public CartResponse addItem(@RequestBody AddCartItemRequest request) {
        return cartService.addItem(request);
    }

    @PatchMapping("/items/{productId}")
    public CartResponse updateItem(
            @PathVariable("productId") Integer productId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItem(productId, request);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable("productId") Integer productId) {
        return cartService.removeItem(productId);
    }
}
