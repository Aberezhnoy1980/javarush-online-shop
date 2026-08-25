package ru.berezhnoy.shop.catalog;

import org.springframework.data.domain.Sort;

public enum ProductSort {
    NAME(Sort.by("name").ascending()),
    PRICE_ASC(Sort.by("price").ascending()),
    PRICE_DESC(Sort.by("price").descending());

    private final Sort sort;

    ProductSort(Sort sort) {
        this.sort = sort;
    }

    public Sort sort() {
        return sort;
    }
}
