package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.ValidCurrencyCode;
import com.familymoney.domains.transactions.validations.ValidGroupName;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequestDto(
    @NotNull @ValidGroupName String name,
    @NotNull String description,
    @NotNull @ValidCurrencyCode String currencyCode) {}
