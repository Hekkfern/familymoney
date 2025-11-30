package com.familymoney.familymoney.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.lang.NonNull;

public class FakeGenerator {

  private static final String LOWER_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPER_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String NUMBER_CHARACTERS = "0123456789";
  private static final RandomStringUtils random = RandomStringUtils.insecure();

  public static @NonNull String username() {
    return random.next(10, LOWER_CHARACTERS);
  }

  public static @NonNull String email() {
    return random.next(12, LOWER_CHARACTERS) + "@gmail.com";
  }

  public static @NonNull String password() {
    return random.next(12, UPPER_CHARACTERS)
        + random.next(4, LOWER_CHARACTERS)
        + random.next(4, NUMBER_CHARACTERS)
        + "!";
  }
}
