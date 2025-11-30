package com.familymoney.familymoney.types;

import org.springframework.lang.NonNull;

public record Password(String value) {

  @Override
  public @NonNull String toString() {
    return value;
  }
}
