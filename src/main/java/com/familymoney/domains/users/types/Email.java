package com.familymoney.domains.users.types;

import java.util.Locale;
import java.util.Objects;

public record Email(String value) {

  private static final int MAX_LENGTH = 254;
  private static final int MIN_LENGTH = 4;

  public Email {
    value = Objects.requireNonNull(value, "Email cannot be null").toLowerCase(Locale.ROOT);
    if (value.length() > MAX_LENGTH || value.length() < MIN_LENGTH) {
      throw new IllegalArgumentException(
          "Email length must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
    }
  }

  public static Email fromString(final String value) {
    return new Email(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
