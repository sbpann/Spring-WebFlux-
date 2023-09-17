package io.argonlab.fina.controller;

import io.argonlab.fina.controller.dto.ExchangeRateResponse;
import io.argonlab.fina.exception.NotFoundException;
import io.argonlab.fina.services.ExchangeRateService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("exchangeRate")
class ExchangeRateController {
  private final ExchangeRateService exchangeRateService;

  public ExchangeRateController(ExchangeRateService exchangeRateService) {
    this.exchangeRateService = exchangeRateService;
  }

  @GetMapping(produces = "application/json")
  public Mono<ExchangeRateResponse> getCurrency(
      @RequestParam(name = "currency") String currency,
      @RequestParam(name = "targetCurrency") String targetCurrency) {
    return exchangeRateService
        .findExchangeRate(currency, targetCurrency, false)
        .doOnSuccess(
            unused -> {
              List<String> availableCurrencies = ExchangeRateService.getAvailableCurrencies();
              if (!availableCurrencies.contains(currency)) {
                ExchangeRateService.addCurrency(currency);
              }
              if (!availableCurrencies.contains(targetCurrency)) {
                ExchangeRateService.addCurrency(targetCurrency);
              }
            })
        .map(
            x ->
                ExchangeRateResponse.builder()
                    .currency(currency)
                    .targetCurrency(targetCurrency)
                    .rate(x)
                    .build())
        .onErrorResume(unused -> Mono.error(new NotFoundException()));
  }
}
