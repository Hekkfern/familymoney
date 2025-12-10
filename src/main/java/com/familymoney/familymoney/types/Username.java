package com.familymoney.familymoney.types;

import org.jspecify.annotations.NonNull;

public record Username(String value) {

  public static @NonNull Username of(@NonNull String value) {
    return new Username(value);
  }

  @Override
  public @NonNull String toString() {
    return value;
  }
}
