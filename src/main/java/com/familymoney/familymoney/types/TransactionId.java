package com.familymoney.familymoney.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;

public record TransactionId(UUID value) {

  @JsonValue
  public UUID value() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static TransactionId fromString(String value) {
    return new TransactionId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
