package ru.berezhnoy.shop.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public PageResponse<ProductCatalogItemResponse> search(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "brandId", required = false) Integer brandId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", defaultValue = "NAME") ProductSort sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size
    ) {
        ProductFilter filter = new ProductFilter(query, categoryId, brandId, minPrice, maxPrice, sort, page, size);
        return catalogService.search(filter);
    }

    @GetMapping("/products/{productId}")
    public ProductDetailsResponse getProduct(@PathVariable("productId") Integer productId) {
        return catalogService.getProduct(productId);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalogService.listCategories();
    }

    @GetMapping("/brands")
    public List<BrandResponse> brands() {
        return catalogService.listBrands();
    }
}
