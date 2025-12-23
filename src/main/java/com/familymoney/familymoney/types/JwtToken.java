package com.familymoney.familymoney.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record JwtToken(String value) {

  private static final Pattern VALIDATION_PATTERN =
      Pattern.compile("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$");

  public JwtToken {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static JwtToken fromString(String value) {
    return new JwtToken(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
