package io.argonlab.fina.source;

import io.argonlab.fina.services.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeRateSourceFactory {

  private final List<ExchangeCurrencySource> sourceList;
  private final Logger logger = LoggerFactory.getLogger(ExchangeCurrencySource.class);

  @Autowired
  public ExchangeRateSourceFactory(List<ExchangeCurrencySource> sourceList) {
    this.sourceList = sourceList;
    logger.info("Exchange rate sources: {}", sourceList);
  }

  public ExchangeCurrencySource getSource(String sourceName) {

    Optional<ExchangeCurrencySource> exchangeCurrencySource =
        sourceList.stream().filter(source -> source.getName().equals(sourceName)).findFirst();
    if (exchangeCurrencySource.isEmpty()) {
      logger.error("Cannot found exchange rate source %s. returning null.");
      return null;
    }
    return exchangeCurrencySource.get();
  }
}
