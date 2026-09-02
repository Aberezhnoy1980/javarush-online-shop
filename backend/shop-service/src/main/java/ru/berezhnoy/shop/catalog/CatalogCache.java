package ru.berezhnoy.shop.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class CatalogCache {

    private static final Logger log = LoggerFactory.getLogger(CatalogCache.class);
    private static final String PRODUCT_KEY_PREFIX = "online-shop:product:";
    private static final String CATALOG_KEY_PREFIX = "online-shop:catalog:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public CatalogCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${shop.cache.ttl:5m}") Duration ttl
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    public Optional<PageResponse<ProductCatalogItemResponse>> getCatalog(String queryHash) {
        return read(CATALOG_KEY_PREFIX + queryHash, new TypeReference<>() {
        });
    }

    public void putCatalog(String queryHash, PageResponse<ProductCatalogItemResponse> value) {
        write(CATALOG_KEY_PREFIX + queryHash, value);
    }

    public Optional<ProductDetailsResponse> getProduct(Integer productId) {
        return read(PRODUCT_KEY_PREFIX + productId, new TypeReference<>() {
        });
    }

    public void putProduct(Integer productId, ProductDetailsResponse value) {
        write(PRODUCT_KEY_PREFIX + productId, value);
    }

    private <T> Optional<T> read(String key, TypeReference<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception exception) {
            log.warn("Redis cache read failed for key {}: {}", key, exception.getMessage());
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize cache value for key {}: {}", key, exception.getMessage());
        } catch (Exception exception) {
            log.warn("Redis cache write failed for key {}: {}", key, exception.getMessage());
        }
    }
}
