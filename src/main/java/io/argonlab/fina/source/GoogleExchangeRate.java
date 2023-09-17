package io.argonlab.fina.source;

import io.argonlab.fina.exception.NotFoundException;
import io.argonlab.fina.exception.UnknownExchangeRate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@ApplicationScope
@Component
class GoogleExchangeRate extends BaseExchangeRateSource {

  private static final Logger logger = LoggerFactory.getLogger(GoogleExchangeRate.class);
  private final HttpClient httpClient;

  @Autowired
  public GoogleExchangeRate(RedisTemplate<String, Object> redisTemplate) {
    super(redisTemplate, "google");
    httpClient = HttpClient.create();
  }

  @Override
  public Mono<BigDecimal> findExchangeRate(
      String currency, String targetCurrency, boolean forceUpdate) {
    String cacheKey = String.format("%s.%s.google", currency, targetCurrency);
    Object cachedValue = getRedisTemplate().opsForValue().get(cacheKey);
    if (cachedValue != null && !forceUpdate) {
      if ("unknown".equals(cachedValue)) {
        return Mono.error(new NotFoundException());
      }
      logger.info("Found {} in cache.", cachedValue);
      return Mono.just((BigDecimal) cachedValue);
    }
    Mono<String> uri =
        Mono.just(
            String.format("https://www.google.com/finance/quote/%s-%s", currency, targetCurrency));
    return httpClient
        .get()
        .uri(uri)
        .responseSingle(
            (response, content) -> {
              if (response.status().code() != 200) {
                logger.warn(
                    "Cannot connect to Google server, status: {}.", response.status().code());
                return Mono.error(new NotFoundException());
              }
              return content.asString();
            })
        .flatMap(
            html -> {
              Document doc = Jsoup.parse(html);
              Element priceElement = doc.selectFirst("[data-last-price]");

              if (priceElement == null) {
                logger.info("Unknown the exchange rate {}. Set to {}.", cacheKey, "unknown");
                getRedisTemplate().opsForValue().set(cacheKey, "unknown");
                return Mono.error(new UnknownExchangeRate(currency, targetCurrency));
              }

              String priceString = priceElement.attr("data-last-price");
              BigDecimal price = new BigDecimal(priceString);
              getRedisTemplate().opsForValue().set(cacheKey, price, 1, TimeUnit.MINUTES);
              return Mono.just(price);
            })
        .retry(3)
        .onErrorMap(
            throwable -> {
              logger.warn(
                  "Found error on searching {}. message: {}", cacheKey, throwable.getMessage());
              return new NotFoundException();
            });
  }
}
