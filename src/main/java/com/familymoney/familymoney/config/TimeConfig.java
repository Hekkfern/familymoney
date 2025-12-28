package com.familymoney.familymoney.config;

import java.time.ZoneOffset;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {
  @Bean
  public java.time.Clock getClock() {
    return java.time.Clock.systemUTC();
  }

  @Bean
  public io.jsonwebtoken.Clock getJwtClock() {
    return () -> java.util.Date.from(java.time.Instant.now(getClock()));
  }

  @Bean
  public ZoneOffset zone() {
    return ZoneOffset.UTC;
  }
}
