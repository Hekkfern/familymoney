package com.familymoney.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordEncoder {

  private static final int STRENGTH = 12;

  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(STRENGTH);

  public String encode(String rawPassword) {
    return bcrypt.encode(rawPassword);
  }

  public boolean verify(String rawPassword, String hashedPassword) {
    return bcrypt.matches(rawPassword, hashedPassword);
  }
}
