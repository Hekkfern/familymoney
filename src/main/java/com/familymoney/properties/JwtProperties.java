package com.familymoney.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for JWT (JSON Web Token) settings.
 *
 * <p>This class holds the configuration properties related to JWT, such as the secret key used for
 * signing and verifying tokens. The properties are validated to ensure that they meet the required
 * constraints, such as being non-blank and having a minimum length for security purposes.
 */
@ConfigurationProperties(prefix = "familymoney.jwt")
@Validated
public record JwtProperties(
    @NotBlank @Size(min = 48) String key,
    @NotNull @DurationMin(minutes = 5) @DurationMax(minutes = 15) Duration accessTokenDuration,
    @NotNull @DurationMin(hours = 1) @DurationMax(days = 7) Duration refreshTokenDuration) {}
