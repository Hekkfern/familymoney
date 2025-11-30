package com.familymoney.familymoney.types;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record RefreshToken(String value) {

  @Override
  public @NonNull String toString() {
    return value;
  }

  @NonNull
  public static RefreshToken generate() {
    return new RefreshToken(UUID.randomUUID().toString().replace("-", ""));
  }
}
