package ru.berezhnoy.shop.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogCacheTest {

    @Test
    void redisOutageReturnsEmptyInsteadOfFailing() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenThrow(new RuntimeException("Redis is down"));

        CatalogCache cache = new CatalogCache(redisTemplate, new ObjectMapper(), Duration.ofMinutes(5));

        Optional<ProductDetailsResponse> product = cache.getProduct(1);
        Optional<PageResponse<ProductCatalogItemResponse>> catalog = cache.getCatalog("abc");

        assertThat(product).isEmpty();
        assertThat(catalog).isEmpty();

        ProductDetailsResponse details = new ProductDetailsResponse(
                1, "Test", "desc", BigDecimal.TEN, 1, 1, "Brand", 1, "Cat"
        );
        cache.putProduct(1, details);
        cache.putCatalog("abc", new PageResponse<>(List.of(), 0, 12, 0, 0));
    }
}
