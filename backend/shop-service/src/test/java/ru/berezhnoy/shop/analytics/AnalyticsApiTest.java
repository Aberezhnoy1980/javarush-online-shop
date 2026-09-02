package ru.berezhnoy.shop.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.berezhnoy.shop.PostgresTestConfiguration;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class AnalyticsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void topProductsIgnoreUnpaidOrdersAndUseDatabaseAggregates() throws Exception {
        mockMvc.perform(get("/api/analytics/top-products")
                        .param("from", "2020-01-01")
                        .param("to", "2099-12-31")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("iPhone 15 Pro Max"))
                .andExpect(jsonPath("$[0].unitsSold").value(2))
                .andExpect(jsonPath("$[0].revenue").value(comparesEqualTo(new BigDecimal("209970.00").doubleValue())))
                .andExpect(jsonPath("$[0].averageRating").value(comparesEqualTo(5.0)))
                .andExpect(jsonPath("$[?(@.productName=='AirPods Pro')].unitsSold").value(hasItem(1)));
    }

    @Test
    void salesSummaryGroupsRevenueByCategoryAndCountsAllStatuses() throws Exception {
        mockMvc.perform(get("/api/analytics/sales-summary")
                        .param("from", "2020-01-01")
                        .param("to", "2099-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories.length()").value(3))
                .andExpect(jsonPath("$.orderStatuses[?(@.status=='NEW')].orderCount").value(hasItem(1)))
                .andExpect(jsonPath("$.orderStatuses[?(@.status=='DELIVERED')].orderCount").value(hasItem(2)))
                .andExpect(jsonPath("$.orderStatuses[?(@.status=='PAID')].orderCount").value(hasItem(1)))
                .andExpect(jsonPath("$.orderStatuses[?(@.status=='PROCESSING')].orderCount").value(hasItem(1)));
    }

    @Test
    void periodAndLimitAreValidated() throws Exception {
        mockMvc.perform(get("/api/analytics/top-products")
                        .param("from", "2026-12-31")
                        .param("to", "2026-01-01"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/analytics/top-products").param("limit", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/analytics/top-products")
                        .param("from", "2099-01-01")
                        .param("to", "2099-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
