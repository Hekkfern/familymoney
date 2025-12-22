package com.familymoney.familymoney.properties;

import com.familymoney.familymoney.types.UserName;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "admin")
@Validated
public record AdminProperties(
    @NotNull UserName username,
    @NotNull com.familymoney.familymoney.types.Email email,
    @NotNull com.familymoney.familymoney.types.Password password) {}
