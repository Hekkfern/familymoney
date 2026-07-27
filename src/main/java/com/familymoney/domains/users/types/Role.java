package com.familymoney.domains.users.types;

public enum Role {
  USER,
  ADMIN;

  public static Role fromString(String role) {
    return switch (role) {
      case "USER" -> Role.USER;
      case "ADMIN" -> Role.ADMIN;
      default -> throw new IllegalArgumentException("Unknown role: " + role);
    };
  }

  @Override
  public String toString() {
    return switch (this) {
      case USER -> "USER";
      case ADMIN -> "ADMIN";
    };
  }
}
