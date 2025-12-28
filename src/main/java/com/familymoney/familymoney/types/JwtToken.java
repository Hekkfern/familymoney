package com.familymoney.familymoney.types;


public record JwtToken(String value) {

  public static JwtToken fromString(String value) {
    return new JwtToken(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
