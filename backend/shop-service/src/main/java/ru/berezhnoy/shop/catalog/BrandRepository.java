package ru.berezhnoy.shop.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berezhnoy.shop.domain.Brand;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    List<Brand> findAllByOrderByNameAsc();
}
