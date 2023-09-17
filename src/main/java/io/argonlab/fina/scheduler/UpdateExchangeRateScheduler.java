package io.argonlab.fina.scheduler;

import io.argonlab.fina.services.ExchangeRateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class UpdateExchangeRateScheduler {
  private static final Logger logger = LoggerFactory.getLogger(UpdateExchangeRateScheduler.class);
  private final ExchangeRateService exchangeRateService;

  @Autowired
  public UpdateExchangeRateScheduler(ExchangeRateService exchangeRateService) {
    this.exchangeRateService = exchangeRateService;
  }

  @Scheduled(fixedRate = 30000)
  public void update() {
    Instant start = Instant.now();
    exchangeRateService
        .update()
        .subscribe(
            updateResult -> {
              logger.info("Updated: {}", updateResult);
            },
            throwable -> {
              logger.error("Cannot update", throwable);
            },
            () -> {
              logger.info(
                  "Updated completed. execute time: {} seconds.",
                  Duration.between(start, Instant.now()).toSeconds());
            });
  }

  @Scheduled(fixedRate = 3_600_000)
  public void clearUserInputCurrency() {
    ExchangeRateService.clearCurrency();
  }
}
