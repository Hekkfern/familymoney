package com.familymoney.familymoney.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for JWT (JSON Web Token) settings.
 *
 * <p>This class holds the configuration properties related to JWT, such as the secret key used for
 * signing and verifying tokens. The properties are validated to ensure that they meet the required
 * constraints, such as being non-blank and having a minimum length for security purposes.
 */
@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(@NotBlank @Size(min = 48) String key) {}
