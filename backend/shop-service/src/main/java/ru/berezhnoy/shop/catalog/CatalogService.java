package ru.berezhnoy.shop.catalog;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berezhnoy.shop.domain.Brand;
import ru.berezhnoy.shop.domain.Category;
import ru.berezhnoy.shop.domain.Product;
import ru.berezhnoy.shop.web.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class CatalogService {

    private static final int SHORT_DESCRIPTION_LIMIT = 120;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CatalogCache catalogCache;

    public CatalogService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            CatalogCache catalogCache
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.catalogCache = catalogCache;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductCatalogItemResponse> search(ProductFilter filter) {
        String cacheKey = catalogKey(filter);
        return catalogCache.getCatalog(cacheKey).orElseGet(() -> {
            Page<ProductCatalogItemResponse> page = productRepository
                    .findAll(ProductSpecifications.from(filter), filter.pageable())
                    .map(this::toCatalogItem);
            PageResponse<ProductCatalogItemResponse> response = PageResponse.from(page);
            catalogCache.putCatalog(cacheKey, response);
            return response;
        });
    }

    @Transactional(readOnly = true)
    public ProductDetailsResponse getProduct(Integer productId) {
        return catalogCache.getProduct(productId).orElseGet(() -> {
            Product product = productRepository.findOneById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not found"));
            ProductDetailsResponse response = toDetails(product);
            catalogCache.putProduct(productId, response);
            return response;
        });
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllWithParent().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getParent() == null ? null : category.getParent().getId()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> listBrands() {
        return brandRepository.findAllByOrderByNameAsc().stream()
                .map(brand -> new BrandResponse(brand.getId(), brand.getName()))
                .toList();
    }

    private ProductCatalogItemResponse toCatalogItem(Product product) {
        return new ProductCatalogItemResponse(
                product.getId(),
                product.getName(),
                shorten(product.getDescription()),
                product.getPrice(),
                product.getStockQuantity(),
                nameOf(product.getBrand()),
                nameOf(product.getCategory())
        );
    }

    private ProductDetailsResponse toDetails(Product product) {
        Brand brand = product.getBrand();
        Category category = product.getCategory();
        return new ProductDetailsResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                brand == null ? null : brand.getId(),
                nameOf(brand),
                category == null ? null : category.getId(),
                nameOf(category)
        );
    }

    private static String nameOf(Brand brand) {
        return brand == null ? null : brand.getName();
    }

    private static String nameOf(Category category) {
        return category == null ? null : category.getName();
    }

    private static String shorten(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }
        String trimmed = description.trim();
        if (trimmed.length() <= SHORT_DESCRIPTION_LIMIT) {
            return trimmed;
        }
        return trimmed.substring(0, SHORT_DESCRIPTION_LIMIT).trim() + "…";
    }

    static String catalogKey(ProductFilter filter) {
        String normalized = String.join("|",
                nullToEmpty(filter.query()).toLowerCase(),
                String.valueOf(filter.categoryId()),
                String.valueOf(filter.brandId()),
                String.valueOf(filter.minPrice()),
                String.valueOf(filter.maxPrice()),
                filter.sort().name(),
                String.valueOf(filter.page()),
                String.valueOf(filter.size())
        );
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for catalog cache keys", exception);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
