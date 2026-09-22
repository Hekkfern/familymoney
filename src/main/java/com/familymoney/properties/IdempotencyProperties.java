package com.familymoney.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "familymoney.idempotency")
@Validated
public record IdempotencyProperties(
    @NotNull @DurationMin(minutes = 1) @DurationMax(hours = 1) Duration keyDuration,
    @NotNull @Min(1) int deleteBatchSize) {}
