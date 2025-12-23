package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.validation.ValidCurrencyCode;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequestDto(
    GroupName name, @NotNull String description, @ValidCurrencyCode String currencyCode) {}
