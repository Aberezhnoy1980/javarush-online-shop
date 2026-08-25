package ru.berezhnoy.shop.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.berezhnoy.shop.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUser_IdOrderByCreatedAtDesc(Integer userId);

    @EntityGraph(attributePaths = {"items", "items.product", "payment"})
    Optional<Order> findByIdAndUser_Id(Integer id, Integer userId);
}
