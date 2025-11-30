package com.familymoney.familymoney.types;

import org.jspecify.annotations.NonNull;

public record Email(String value) {

  @Override
  public @NonNull String toString() {
    return value;
  }
}
