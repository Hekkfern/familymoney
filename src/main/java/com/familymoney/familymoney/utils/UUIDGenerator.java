package com.familymoney.familymoney.utils;

import com.github.f4b6a3.uuid.alt.GUID;
import java.util.UUID;

public class UUIDGenerator {
  private UUIDGenerator() {
    /* This utility class should not be instantiated */
  }

  public static UUID generate() {
    return GUID.v7().toUUID();
  }
}
