package ru.berezhnoy.shop.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

public class RestClientPaymentGateway implements PaymentGateway {

    private final RestClient paymentRestClient;

    public RestClientPaymentGateway(RestClient paymentRestClient) {
        this.paymentRestClient = paymentRestClient;
    }

    @Override
    public PaymentGatewayResponse charge(Integer orderId, BigDecimal amount, String paymentMethod) {
        try {
            return paymentRestClient.post()
                    .uri("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "orderId", orderId,
                            "amount", amount,
                            "paymentMethod", paymentMethod
                    ))
                    .retrieve()
                    .onStatus(this::isUnavailable, (request, response) -> {
                        throw new PaymentUnavailableException("Payment service is temporarily unavailable");
                    })
                    .body(PaymentGatewayResponse.class);
        } catch (PaymentUnavailableException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new PaymentUnavailableException("Payment service is unreachable", exception);
        } catch (RestClientResponseException exception) {
            throw new PaymentUnavailableException("Payment service rejected the charge: " + exception.getStatusCode(), exception);
        }
    }

    @Override
    public PaymentGatewayResponse cancel(String paymentId) {
        try {
            return paymentRestClient.post()
                    .uri("/api/payments/{paymentId}/cancel", paymentId)
                    .retrieve()
                    .onStatus(this::isUnavailable, (request, response) -> {
                        throw new PaymentUnavailableException("Payment service is temporarily unavailable");
                    })
                    .body(PaymentGatewayResponse.class);
        } catch (PaymentUnavailableException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new PaymentUnavailableException("Payment service is unreachable", exception);
        } catch (RestClientResponseException exception) {
            throw new PaymentUnavailableException("Payment service rejected the cancel: " + exception.getStatusCode(), exception);
        }
    }

    private boolean isUnavailable(HttpStatusCode status) {
        return status == HttpStatus.SERVICE_UNAVAILABLE;
    }
}
