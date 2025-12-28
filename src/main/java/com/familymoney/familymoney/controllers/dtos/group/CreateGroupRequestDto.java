package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.validation.ValidCurrencyCode;
import com.familymoney.familymoney.validation.ValidGroupName;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequestDto(
    @NotNull @ValidGroupName String name,
    @NotNull String description,
    @NotNull @ValidCurrencyCode String currencyCode) {}
