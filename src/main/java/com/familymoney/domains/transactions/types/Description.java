package com.familymoney.domains.transactions.types;

public record Description(String value) {

  public static Description of(final String value) {
    return new Description(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
