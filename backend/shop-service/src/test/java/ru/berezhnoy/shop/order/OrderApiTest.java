package ru.berezhnoy.shop.order;

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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class OrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listAndGetOwnOrdersHideOtherUsers() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productName").exists())
                .andExpect(jsonPath("$.payment.status").value("PENDING"));

        mockMvc.perform(get("/api/orders/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutSnapshotsItemsAndClearsCart() throws Exception {
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totalAmount").value(comparesEqualTo(new BigDecimal("249970.00").doubleValue())))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.payment.status").value("PENDING"))
                .andExpect(jsonPath("$.payment.paymentMethod").value("CARD"));

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void emptyCartCannotBeCheckedOut() throws Exception {
        mockMvc.perform(delete("/api/cart/items/1")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/cart/items/8")).andExpect(status().isOk());

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cart is empty"));
    }
}
