package com.familymoney.familymoney.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.application")
@Validated
@Builder
public record AppProperties(@NotBlank String name) {}
