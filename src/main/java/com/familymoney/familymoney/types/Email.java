package com.familymoney.familymoney.types;

import java.util.regex.Pattern;

public record Email(String value) {

  private static final Pattern VALIDATION_PATTERN =
      Pattern.compile(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

  public Email {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid email address");
    }
  }

  @Override
  public String toString() {
    return value;
  }

  public static Email fromString(String value) {
    return new Email(value);
  }
}
