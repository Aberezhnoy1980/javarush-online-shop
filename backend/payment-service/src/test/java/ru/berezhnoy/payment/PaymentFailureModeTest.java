package ru.berezhnoy.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "payment.failure-rate=1")
@AutoConfigureMockMvc
class PaymentFailureModeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void configuredOutageReturns503() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":1,\"amount\":100.00,\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
