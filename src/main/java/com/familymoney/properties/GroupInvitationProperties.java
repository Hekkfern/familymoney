package com.familymoney.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "familymoney.group-invitation")
@Validated
public record GroupInvitationProperties(
    @NotNull @DurationMin(minutes = 5) @DurationMax(hours = 1) Duration invitationDuration,
    @NotNull @Min(1) @Max(5) Integer maxNumInvitations) {}
