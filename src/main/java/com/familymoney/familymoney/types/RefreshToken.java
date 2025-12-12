package com.familymoney.familymoney.types;

import java.util.UUID;

public record RefreshToken(String value) {

  @Override
  public String toString() {
    return value;
  }

  public static RefreshToken generate() {
    return new RefreshToken(UUID.randomUUID().toString().replace("-", ""));
  }
}
