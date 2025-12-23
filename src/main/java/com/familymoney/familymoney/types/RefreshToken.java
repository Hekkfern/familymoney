package com.familymoney.familymoney.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;
import java.util.regex.Pattern;

public record RefreshToken(String value) {

  public static final int LENGTH = 32;
  private static final Pattern VALIDATION_PATTERN =
      Pattern.compile(String.format("^[A-Za-z0-9]{%s}$", LENGTH));

  public RefreshToken {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static RefreshToken fromString(String value) {
    return new RefreshToken(value);
  }

  public static RefreshToken generate() {
    return new RefreshToken(UUID.randomUUID().toString().replace("-", ""));
  }

  @Override
  public String toString() {
    return value;
  }
}
