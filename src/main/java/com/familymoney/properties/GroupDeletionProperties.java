package com.familymoney.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the delayed, batched purge of soft-deleted groups.
 *
 * @param retention the minimum time a soft-deleted group is kept before it is purged
 * @param deleteBatchSize the maximum number of rows deleted by each purge statement
 */
@ConfigurationProperties(prefix = "familymoney.group-deletion")
@Validated
public record GroupDeletionProperties(
    @NotNull @DurationMin(hours = 1) @DurationMax(days = 30) Duration retention,
    @NotNull @Min(1) int deleteBatchSize) {}
