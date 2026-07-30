package com.familymoney.properties;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for the Email Verification system for new users. */
@ConfigurationProperties(prefix = "familymoney.email-verification")
@Validated
public record EmailVerificationProperties(
    @NotNull @DurationMin(hours = 1) @DurationMax(hours = 48) Duration tokenDuration,
    @NotNull @DurationMin(minutes = 1) @DurationMax(minutes = 15) Duration waitTime) {}
