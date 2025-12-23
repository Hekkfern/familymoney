package com.familymoney.familymoney.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record PasswordResetToken(String value) {

  private static final Pattern VALIDATION_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{2,31}$");

  public PasswordResetToken {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static PasswordResetToken fromString(String value) {
    return new PasswordResetToken(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
