package io.argonlab.fina.services;

import io.argonlab.fina.exception.NotFoundException;
import io.argonlab.fina.source.ExchangeRateSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

public class ExchangeRateService {

  private final ExchangeRateSourceFactory exchangeRateSourceFactory;
  private static final List<String> MAIN_CURRENCY_LIST =
      List.of("USD", "NZD", "EUR", "GBP", "AUD", "JPY", "CAD", "CHF", "CNY", "HKD", "SGD", "TWD");
  private static final List<String> ADDITIONAL_CURRENCY_LIST = new ArrayList<>();
  private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

  public ExchangeRateService(ExchangeRateSourceFactory exchangeRateSourceFactory) {
    this.exchangeRateSourceFactory = exchangeRateSourceFactory;
  }

  public static List<String> getAvailableCurrencies() {
    return Stream.concat(MAIN_CURRENCY_LIST.stream(), ADDITIONAL_CURRENCY_LIST.stream()).toList();
  }

  public static void addCurrency(String currency) {
    if (!ADDITIONAL_CURRENCY_LIST.contains(currency)) {
      ADDITIONAL_CURRENCY_LIST.add(currency);
    }
  }

  public static void clearCurrency() {
    ADDITIONAL_CURRENCY_LIST.clear();
  }

  public Flux<String> update() {
    List<List<String>> currencyPairs = generateCurrencyPairs();

    return Flux.fromIterable(currencyPairs)
        .flatMap(
            pair ->
                findExchangeRate(pair.get(0), pair.get(1), true)
                    .map(price -> String.format("%s-%s: %s", pair.get(0), pair.get(1), price))
                    .onErrorResume(unused -> Mono.empty()));
  }

  public List<List<String>> generateCurrencyPairs() {
    List<String> availableCurrencies = getAvailableCurrencies();
    List<List<String>> pairs = new ArrayList<>();

    for (int i = 0; i < availableCurrencies.size(); i++) {
      String currency = availableCurrencies.get(i);

      for (int j = i + 1; j < availableCurrencies.size(); j++) {
        String targetCurrency = availableCurrencies.get(j);

        if (!currency.equals(targetCurrency)) {
          List<String> pair = List.of(currency, targetCurrency);
          pairs.add(pair);
        }
      }
    }

    return pairs;
  }

  public Mono<Map<String, BigDecimal>> findExchangeRate(
      String currency, String targetCurrency, boolean forceUpdate) {
    logger.info("Finding exchange rate {} to {}.", currency, targetCurrency);
    Currency currencyInstance;
    Currency targetCurrencyInstance;
    try {
      currencyInstance = Currency.getInstance(currency);
      targetCurrencyInstance = Currency.getInstance(targetCurrency);
    } catch (Exception e) {
      return Mono.error(new NotFoundException());
    }
    return Flux.fromIterable(List.of("google"))
        .flatMap(
            sourceName ->
                exchangeRateSourceFactory
                    .getSource(sourceName)
                    .findExchangeRate(
                        currencyInstance.getCurrencyCode(),
                        targetCurrencyInstance.getCurrencyCode(),
                        forceUpdate)
                    .map(rate -> Map.of(sourceName, rate)))
        .collectList()
        .map(
            rateList -> {
              Map<String, BigDecimal> mergedRateList = new HashMap<>();
              for (Map<String, BigDecimal> rate : rateList) {
                mergedRateList.putAll(rate);
              }
              return mergedRateList;
            });
  }
}
