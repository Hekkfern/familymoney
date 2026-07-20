package com.familymoney.properties;

import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.UserName;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the default admin user.
 *
 * <p>These properties are used to create the first admin user on application startup if no admin
 * users exist. The properties include the username, email, and password for the admin user. All
 * fields are required and validated to ensure that a valid admin user can be created.
 */
@ConfigurationProperties(prefix = "familymoney.admin")
@Validated
public record AdminProperties(
    @NotNull UserName username, @NotNull Email email, @NotNull Password password) {}
