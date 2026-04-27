package com.familymoney.controllers.dtos.grouptransaction;

import com.familymoney.validation.ValidCurrencyCode;
import com.familymoney.validation.ValidGroupName;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequestDto(
    @NotNull @ValidGroupName String name,
    @NotNull String description,
    @NotNull @ValidCurrencyCode String currencyCode) {}
