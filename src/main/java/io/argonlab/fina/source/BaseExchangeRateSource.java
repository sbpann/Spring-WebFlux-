package io.argonlab.fina.source;

import org.springframework.data.redis.core.RedisTemplate;

abstract class BaseExchangeRateSource implements ExchangeCurrencySource {
  private final RedisTemplate<String, Object> redisTemplate;
  private final String name;

  BaseExchangeRateSource(RedisTemplate<String, Object> redisTemplate, String name) {
    this.redisTemplate = redisTemplate;
    this.name = name;
  }

  RedisTemplate<String, Object> getRedisTemplate() {
    return redisTemplate;
  }

  @Override
  public String getName() {
    return name;
  }
}
