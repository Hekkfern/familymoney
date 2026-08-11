package com.familymoney.domains.auth.types;

import java.util.Objects;
import java.util.regex.Pattern;

/** Value object representing an access token. */
public record AccessToken(String value) {

  private static final Pattern ACCESS_TOKEN_PATTERN =
      Pattern.compile(
          "^e[yw][A-Za-z0-9-_]+\\.(?:e[yw][A-Za-z0-9-_]+)?\\.[A-Za-z0-9-_]{2,}(?:(?:\\.[A-Za-z0-9-_]{2,}){2})?$");

  /**
   * Canonical constructor which validates the token value.
   *
   * @param value the token string, must not be null or empty and must match the expected pattern
   * @throws NullPointerException if {@code value} is null
   * @throws IllegalArgumentException if {@code value} is empty or does not match the token pattern
   */
  public AccessToken {
    Objects.requireNonNull(value, "Access token cannot be null or empty");
    if (value.isEmpty()) {
      throw new IllegalArgumentException("Access token cannot be null or empty");
    }
    if (!ACCESS_TOKEN_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Access token has invalid JWT Token format");
    }
  }

  /**
   * Create an {@link AccessToken} from a string.
   *
   * @param value the token string
   * @return a validated {@link AccessToken}
   */
  public static AccessToken fromString(final String value) {
    return new AccessToken(value);
  }

  /**
   * Return the string representation of the value.
   *
   * @return the token string
   */
  @Override
  public String toString() {
    return value;
  }
}
