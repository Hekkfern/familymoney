package com.familymoney.domains.transactions.types;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/** Value object representing an expiration time. */
public record ExpirationTime(Instant value) {

  /**
   * Canonical constructor which validates the value.
   *
   * @param value the expiration time, must not be null
   * @throws NullPointerException if {@code value} is null
   */
  public ExpirationTime {
    Objects.requireNonNull(value, "Expiration time cannot be null");
  }

  /**
   * Creates an expiration time from the specified instant.
   *
   * @param value the expiration time, must not be null
   * @return an expiration time containing the specified instant
   * @throws NullPointerException if {@code value} is null
   */
  public static ExpirationTime of(final Instant value) {
    return new ExpirationTime(value);
  }

  /**
   * Creates an expiration time from the specified offset date-time.
   *
   * @param value the expiration time, must not be null
   * @return an expiration time containing the specified instant
   * @throws NullPointerException if {@code value} is null
   */
  public static ExpirationTime of(final OffsetDateTime value) {
    return of(value.toInstant());
  }

  /**
   * Checks whether the expiration time has been reached or passed.
   *
   * @param clock the clock used to determine the current instant, must not be null
   * @return {@code true} if the current instant is after the expiration time, {@code false}
   *     otherwise
   */
  public boolean isExpired(final Clock clock) {
    return Instant.now(clock).isAfter(value);
  }

  /**
   * Return the string representation of the value.
   *
   * @return the expiration time as a string
   */
  @Override
  public String toString() {
    return value.toString();
  }

  /**
   * Converts the expiration time to an {@link OffsetDateTime} in UTC.
   *
   * @return the expiration time as an {@link OffsetDateTime} in UTC
   */
  public OffsetDateTime toOffsetDateTime() {
    return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }
}
