package com.familymoney.domains.user.types;

public record UserName(String value) {

  public static UserName fromString(final String value) {
    return new UserName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
