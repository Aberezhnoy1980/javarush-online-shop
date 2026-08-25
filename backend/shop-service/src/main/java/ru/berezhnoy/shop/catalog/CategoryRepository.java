package ru.berezhnoy.shop.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berezhnoy.shop.domain.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("select c from Category c left join fetch c.parent order by c.id")
    List<Category> findAllWithParent();
}
