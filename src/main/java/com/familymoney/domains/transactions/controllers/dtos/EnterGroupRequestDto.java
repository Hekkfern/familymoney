package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.ValidShareGroupToken;
import jakarta.validation.constraints.NotNull;

public record EnterGroupRequestDto(@NotNull @ValidShareGroupToken String token) {}
