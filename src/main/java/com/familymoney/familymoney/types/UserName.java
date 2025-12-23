package com.familymoney.familymoney.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record UserName(String value) {

  private static final Pattern VALIDATION_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{2,31}$");

  public UserName {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Name must be alphanumeric, '-' and '_', and have a length between 3 and 32 characters");
    }
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static UserName fromString(String value) {
    return new UserName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
