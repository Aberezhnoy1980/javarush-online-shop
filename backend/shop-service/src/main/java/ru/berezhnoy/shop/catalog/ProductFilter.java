package ru.berezhnoy.shop.catalog;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public record ProductFilter(
        String query,
        Integer categoryId,
        Integer brandId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ProductSort sort,
        int page,
        int size
) {

    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 50;

    public ProductFilter {
        sort = sort == null ? ProductSort.NAME : sort;
        page = Math.max(page, 0);
        size = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        query = query == null || query.isBlank() ? null : query.trim();
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }
    }

    public Pageable pageable() {
        return PageRequest.of(page, size, sort.sort());
    }
}
