package io.argonlab.fina.service;

import io.argonlab.fina.services.ExchangeRateService;
import io.argonlab.fina.source.ExchangeCurrencySource;
import io.argonlab.fina.source.ExchangeRateSourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExchangeRateServiceTest {

    private ExchangeRateService exchangeRateService;
    private ExchangeRateSourceFactory exchangeRateSourceFactory;

    @BeforeEach
    void setUp() {
        exchangeRateSourceFactory = mock(ExchangeRateSourceFactory.class);
        exchangeRateService = new ExchangeRateService(exchangeRateSourceFactory);
    }

    @Test
    void testFindExchangeRate() {
        // Arrange
        ExchangeCurrencySource exchangeCurrencySource = mock(ExchangeCurrencySource.class);
        when(exchangeRateSourceFactory.getSource("google"))
                .thenReturn(exchangeCurrencySource);
        when(exchangeCurrencySource.findExchangeRate(eq("USD"),eq("EUR"),eq(false)))
                .thenReturn(Mono.just(new BigDecimal("123.45")));

        // Act
        StepVerifier.create(exchangeRateService.findExchangeRate("USD", "EUR", false))
                .expectNext(Map.of("google", new BigDecimal("123.45")))
                .expectComplete()
                .verify();
    }
}
