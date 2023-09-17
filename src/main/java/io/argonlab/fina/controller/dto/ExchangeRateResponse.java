package io.argonlab.fina.controller.dto;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExchangeRateResponse {
  private String currency;
  private String targetCurrency;
  private Map<String, BigDecimal> rate;
}
