package io.argonlab.fina.controller;

import io.argonlab.fina.exception.GlobalExceptionHandler;
import io.argonlab.fina.exception.NotFoundException;
import io.argonlab.fina.services.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers =  ExchangeRateController.class)
@Import(GlobalExceptionHandler.class)
public class ExchangeRateControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        Mockito.reset(exchangeRateService);
    }

    @Test
    void testGetCurrencySuccess() {
        // Arrange
        when(exchangeRateService.findExchangeRate(anyString(), anyString(), anyBoolean()))
            .thenReturn(Mono.just(Map.of("google",BigDecimal.valueOf(1.2))));

        // Act
        // Assert
        webTestClient.get()
            .uri("/exchangeRate?currency=USD&targetCurrency=EUR")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.rate.google").isEqualTo(1.2);
    }

    @Test
    void testGetCurrencyError() {
        // Arrange
        when(exchangeRateService.findExchangeRate(anyString(), anyString(), anyBoolean()))
            .thenReturn(Mono.error(new NotFoundException()));

        // Act
        // Assert
        webTestClient.get()
            .uri("/exchangeRate?currency=USD&targetCurrency=EUR")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }
}
