package io.argonlab.fina.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfig {
  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setPassword(RedisPassword.of("cache_password"));
    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
    jedisConnectionFactory.afterPropertiesSet();
    try {
      jedisConnectionFactory.getConnection();
    } catch (RedisConnectionFailureException redisConnectionFailureException) {
      throw new RuntimeException(redisConnectionFailureException);
    }
    return jedisConnectionFactory;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(jedisConnectionFactory());
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }
}
