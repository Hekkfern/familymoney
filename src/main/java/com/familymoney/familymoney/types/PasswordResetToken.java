package com.familymoney.familymoney.types;

import java.util.regex.Pattern;

public record PasswordResetToken(String value) {

  private static final Pattern VALIDATION_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{2,31}$");

  public PasswordResetToken {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
  }

  @Override
  public String toString() {
    return value;
  }

  public static PasswordResetToken fromString(String value) {
    return new PasswordResetToken(value);
  }
}
