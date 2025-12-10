package com.familymoney.familymoney.types;

import org.jspecify.annotations.NonNull;

public record Password(String value) {

  public static @NonNull Password of(@NonNull String value) {
    return new Password(value);
  }

  @Override
  public @NonNull String toString() {
    return value;
  }
}
