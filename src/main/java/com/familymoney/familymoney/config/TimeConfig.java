package com.familymoney.familymoney.config;

import java.time.Clock;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {
    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    public ZoneOffset zone() {
        return ZoneOffset.UTC;
    }
}
