package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.types.GroupName;
import jakarta.validation.constraints.NotNull;
import javax.money.CurrencyUnit;

public record CreateGroupRequestDto(
    GroupName name, @NotNull String description, @NotNull String currency) {}
