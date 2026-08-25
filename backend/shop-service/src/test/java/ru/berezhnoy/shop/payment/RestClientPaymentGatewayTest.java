package ru.berezhnoy.shop.payment;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestClientPaymentGatewayTest {

    @Test
    void chargeMapsCompletedResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClientPaymentGateway gateway = new RestClientPaymentGateway(builder.baseUrl("http://payment").build());

        server.expect(requestTo("http://payment/api/payments"))
                .andRespond(withSuccess(
                        """
                                {"id":"pay-1","orderId":1,"amount":10.00,"status":"COMPLETED","transactionId":"txn_1","paymentMethod":"CARD"}
                                """,
                        MediaType.APPLICATION_JSON
                ));

        PaymentGatewayResponse response = gateway.charge(1, BigDecimal.TEN, "CARD");
        assertThat(response.id()).isEqualTo("pay-1");
        server.verify();
    }

    @Test
    void chargeMaps503ToUnavailable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClientPaymentGateway gateway = new RestClientPaymentGateway(builder.baseUrl("http://payment").build());

        server.expect(requestTo("http://payment/api/payments"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> gateway.charge(1, BigDecimal.TEN, "CARD"))
                .isInstanceOf(PaymentUnavailableException.class);
        server.verify();
    }
}
