package com.familymoney.domains.auth.types;

public record AccessToken(String value) {

  public static AccessToken fromString(String value) {
    return new AccessToken(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
