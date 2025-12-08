package com.familymoney.familymoney.types;

import org.jspecify.annotations.NonNull;

public enum Role {
  USER,
  ADMIN;

  public static Role fromString(@NonNull String role) {
    return switch (role) {
      case "USER" -> Role.USER;
      case "ADMIN" -> Role.ADMIN;
      default -> throw new IllegalArgumentException("Unknown role: " + role);
    };
  }

  @Override
  public @NonNull String toString() {
    return switch (this) {
      case USER -> "USER";
      case ADMIN -> "ADMIN";
    };
  }
}
