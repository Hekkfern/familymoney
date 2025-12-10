package com.familymoney.familymoney.types;

import org.jspecify.annotations.NonNull;

public record Email(String value) {

  public static @NonNull Email of(@NonNull String value) {
    return new Email(value);
  }

  @Override
  public @NonNull String toString() {
    return value;
  }
}
