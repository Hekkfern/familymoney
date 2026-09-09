package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.DifferentFromTo;
import com.familymoney.domains.transactions.validations.NotNegativeMoney;
import com.familymoney.domains.transactions.validations.ValidDescription;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
@DifferentFromTo
public record UpdateTransactionRequestDto(
    @Nullable @ValidDescription String description,
    @Nullable UUID from,
    @Nullable UUID to,
    @Nullable @NotNegativeMoney Money amount,
    @Nullable @Past Instant doneAt) {}
