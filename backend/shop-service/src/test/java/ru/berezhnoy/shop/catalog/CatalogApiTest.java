package ru.berezhnoy.shop.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.berezhnoy.shop.PostgresTestConfiguration;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class CatalogApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchFiltersInDatabase() throws Exception {
        mockMvc.perform(get("/api/products").param("query", "macbook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].brandName").exists())
                .andExpect(jsonPath("$.content[0].categoryName").exists());
    }

    @Test
    void searchSortsByPriceAscending() throws Exception {
        mockMvc.perform(get("/api/products").param("sort", "PRICE_ASC").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("AirPods Pro"))
                .andExpect(jsonPath("$.content[0].price").value(comparesEqualTo(new BigDecimal("24990.00").doubleValue())));
    }

    @Test
    void searchFiltersByCategory() throws Exception {
        mockMvc.perform(get("/api/products").param("categoryId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void productDetailsAndMissingProduct() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MacBook Pro 14"))
                .andExpect(jsonPath("$.brandName").value("Apple"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void categoriesAndBrandsAreReturned() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }
}
