package com.familymoney.familymoney.types;

import org.jspecify.annotations.NonNull;

public record PasswordResetToken(String value) {

  @Override
  public @NonNull String toString() {
    return value;
  }
}
