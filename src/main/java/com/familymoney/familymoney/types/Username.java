package com.familymoney.familymoney.types;

import java.util.regex.Pattern;

public record Username(String value) {

  private static final Pattern VALIDATION_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{2,31}$");

  public Username {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Name must be alphanumeric, '-' and '_', and have a length between 3 and 32 characters");
    }
  }

  public static Username fromString(String value) {
    return new Username(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
