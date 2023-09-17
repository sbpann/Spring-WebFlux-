package io.argonlab.fina.exception;

public class UnknownExchangeRate extends Exception {
  private final String currency;
  private final String targetCurrency;

  public UnknownExchangeRate(String currency, String targetCurrency) {
    this.currency = currency;
    this.targetCurrency = targetCurrency;
  }

  @Override
  public String getMessage() {
    return String.format("unknown: %s-%s", currency, targetCurrency);
  }
}
