package com.familymoney.properties;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the password reset system.
 *
 * @param tokenDuration duration for which a reset token remains valid
 * @param waitTime minimum duration between reset email requests
 * @param resetUrl frontend URL receiving the reset token query parameter
 */
@ConfigurationProperties(prefix = "familymoney.reset-password")
@Validated
public record ResetPasswordProperties(
    @NotNull @DurationMin(hours = 1) @DurationMax(hours = 48) Duration tokenDuration,
    @NotNull @DurationMin(minutes = 1) @DurationMax(minutes = 15) Duration waitTime,
    @NotNull URI resetUrl) {}
