package io.argonlab.fina.source;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ExchangeCurrencySource {
  Mono<BigDecimal> findExchangeRate(String currency, String targetCurrency, boolean forceUpdate);

  String getName();
}
