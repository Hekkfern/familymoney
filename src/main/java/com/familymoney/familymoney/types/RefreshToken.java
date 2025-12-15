package com.familymoney.familymoney.types;

import java.util.UUID;
import java.util.regex.Pattern;

public record RefreshToken(String value) {

  private static final Pattern VALIDATION_PATTERN = Pattern.compile("^[A-Za-z0-9]{32}$");

  public RefreshToken {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
  }

  @Override
  public String toString() {
    return value;
  }

  public static RefreshToken fromString(String value) {
    return new RefreshToken(value);
  }

  public static RefreshToken generate() {
    return new RefreshToken(UUID.randomUUID().toString().replace("-", ""));
  }
}
