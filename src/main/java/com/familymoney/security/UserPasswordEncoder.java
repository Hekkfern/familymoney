package com.familymoney.security;

import java.util.Objects;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordEncoder {

  private static final int STRENGTH = 12;

  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(STRENGTH);

  public String encode(final String rawPassword) {
    return Objects.requireNonNull(
        bcrypt.encode(rawPassword), "BCryptPasswordEncoder.encode returned null");
  }

  public boolean verify(final String rawPassword, final String hashedPassword) {
    return bcrypt.matches(rawPassword, hashedPassword);
  }
}
