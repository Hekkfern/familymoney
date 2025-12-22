package com.familymoney.familymoney.types;

import java.util.regex.Pattern;

public record GroupName(String value) {

  private static final Pattern VALIDATION_PATTERN =
      Pattern.compile("^[A-Za-z0-9.-_ =><!?&%()/,]{0,64}$");

  public GroupName {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Name must be alphanumeric, can contain some symbols, and have a max length of 64 characters");
    }
  }

  public static GroupName fromString(String value) {
    return new GroupName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
