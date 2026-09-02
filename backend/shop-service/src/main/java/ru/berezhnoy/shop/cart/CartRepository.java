package ru.berezhnoy.shop.cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.berezhnoy.shop.domain.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findByUser_Id(Integer userId);
}
