package com.familymoney.config;

import java.time.ZoneId;
import java.time.ZoneOffset;

public class Constants {

  private Constants() {
    /* This utility class should not be instantiated */
  }

  public static final ZoneOffset DEFAULT_TIMEZONE_OFFSET = ZoneOffset.UTC;
  public static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("UTC");
}
