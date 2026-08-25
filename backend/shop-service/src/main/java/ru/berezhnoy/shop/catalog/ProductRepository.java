package ru.berezhnoy.shop.catalog;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.berezhnoy.shop.domain.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<Product> findOneById(Integer id);
}

