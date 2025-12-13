package com.familymoney.familymoney.controllers.dtos.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Currency;

public record CreateGroupRequestDto(@NotBlank String name, @NotNull Currency currency) {}
