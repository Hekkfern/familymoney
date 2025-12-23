package com.familymoney.familymoney.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record Password(String value) {

  private static final Pattern VALIDATION_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,64}$");

  public Password {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Password must be between 12 and 64 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@\\$!%*?&)");
    }
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static Password fromString(String value) {
    return new Password(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
