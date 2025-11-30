package com.familymoney.familymoney.types;

import org.springframework.lang.NonNull;

public record JwtToken(String value) {

  @Override
  public @NonNull String toString() {
    return value;
  }
}
