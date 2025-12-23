package com.familymoney.familymoney.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public record ShareGroupToken(String value) {

  public static final int LENGTH = 64;
  private static final Pattern VALIDATION_PATTERN =
      Pattern.compile(String.format("^[A-Za-z0-9]{%s}$", LENGTH));

  public ShareGroupToken {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static ShareGroupToken fromString(String value) {
    return new ShareGroupToken(value);
  }

  public static ShareGroupToken generate() {
    final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      int idx = random.nextInt(CHARSET.length());
      sb.append(CHARSET.charAt(idx));
    }
    return new ShareGroupToken(sb.toString());
  }

  @Override
  public String toString() {
    return value;
  }
}
