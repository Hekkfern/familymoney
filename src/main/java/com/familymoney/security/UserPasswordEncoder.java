package com.familymoney.security;

import java.util.Objects;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordEncoder {

  private static final int STRENGTH = 12;
  private static final String DUMMY_PASSWORD = "dummy-password-879agdfa5gf1ad1216asdds4a98";

  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(STRENGTH);
  private final String dummyPasswordHash = encode(DUMMY_PASSWORD);

  public String encode(final String rawPassword) {
    return Objects.requireNonNull(
        bcrypt.encode(rawPassword), "BCryptPasswordEncoder.encode returned null");
  }

  public boolean verify(final String rawPassword, final String hashedPassword) {
    return bcrypt.matches(rawPassword, hashedPassword);
  }

  public boolean verifyDummyPassword(final String rawPassword) {
    return verify(rawPassword, dummyPasswordHash);
  }
}
