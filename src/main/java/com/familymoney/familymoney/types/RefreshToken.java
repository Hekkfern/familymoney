package com.familymoney.familymoney.types;

import org.springframework.lang.NonNull;
import java.util.UUID;

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
