package com.familymoney.domains.transactions.types;

/**
 * A value object representing a description for a transaction or group. It can be an empty text.
 */
public record Description(String value) {

  public static Description of(final String value) {
    return new Description(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
