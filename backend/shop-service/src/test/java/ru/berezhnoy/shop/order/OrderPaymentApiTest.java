package ru.berezhnoy.shop.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.berezhnoy.shop.PostgresTestConfiguration;
import ru.berezhnoy.shop.payment.PaymentGateway;
import ru.berezhnoy.shop.payment.PaymentGatewayResponse;
import ru.berezhnoy.shop.payment.PaymentUnavailableException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class OrderPaymentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentGateway paymentGateway;

    @Test
    void payThenCancelUpdatesStatuses() throws Exception {
        when(paymentGateway.charge(eq(1), any(), eq("CARD")))
                .thenReturn(new PaymentGatewayResponse(
                        "pay-1", 1, new BigDecimal("249970.00"), "COMPLETED", "txn_1", "CARD"
                ));
        when(paymentGateway.cancel("pay-1"))
                .thenReturn(new PaymentGatewayResponse(
                        "pay-1", 1, new BigDecimal("249970.00"), "REFUNDED", "txn_1", "CARD"
                ));

        mockMvc.perform(post("/api/orders/1/payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.payment.status").value("COMPLETED"))
                .andExpect(jsonPath("$.payment.transactionId").value("pay-1"));

        mockMvc.perform(post("/api/orders/1/payment"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/orders/1/payment/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_CANCELLED"))
                .andExpect(jsonPath("$.payment.status").value("REFUNDED"));
    }

    @Test
    void unavailableGatewayDoesNotMarkPaidAndAllowsRetry() throws Exception {
        when(paymentGateway.charge(eq(1), any(), eq("CARD")))
                .thenThrow(new PaymentUnavailableException("Payment service is temporarily unavailable"))
                .thenReturn(new PaymentGatewayResponse(
                        "pay-retry", 1, new BigDecimal("249970.00"), "COMPLETED", "txn_retry", "CARD"
                ));

        mockMvc.perform(post("/api/orders/1/payment"))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"))
                .andExpect(jsonPath("$.payment.status").value("FAILED"));

        mockMvc.perform(post("/api/orders/1/payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void cancelRejectedUntilPaid() throws Exception {
        mockMvc.perform(post("/api/orders/1/payment/cancel"))
                .andExpect(status().isConflict());
    }
}
