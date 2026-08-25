package ru.berezhnoy.shop.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.berezhnoy.shop.PostgresTestConfiguration;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class CartApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void demoUserCartContainsSeedItems() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.totalAmount").value(comparesEqualTo(new BigDecimal("249970.00").doubleValue())))
                .andExpect(jsonPath("$.items[0].name").value("AirPods Pro"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[1].name").value("MacBook Pro 14"));
    }

    @Test
    void addUpdateAndRemoveItems() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":8,\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3));

        mockMvc.perform(patch("/api/cart/items/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(1));

        mockMvc.perform(delete("/api/cart/items/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(1))
                .andExpect(jsonPath("$.items[0].name").value("MacBook Pro 14"));
    }

    @Test
    void invalidQuantityAndMissingProduct() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":999,\"quantity\":1}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/cart/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":999}"))
                .andExpect(status().isBadRequest());
    }
}
