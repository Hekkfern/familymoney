package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.PositiveMoney;
import com.familymoney.domains.transactions.validations.ValidDescription;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdatePaymentRequestDto(
    @Nullable @ValidDescription String description,
    @Nullable UUID from,
    @Nullable UUID to,
    @Nullable @PositiveMoney Money amount,
    @Nullable @Past Instant doneAt) {}
