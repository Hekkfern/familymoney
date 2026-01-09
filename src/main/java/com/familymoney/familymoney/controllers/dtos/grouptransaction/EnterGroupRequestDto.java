package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import com.familymoney.familymoney.validation.ValidShareGroupToken;
import jakarta.validation.constraints.NotNull;

public record EnterGroupRequestDto(@NotNull @ValidShareGroupToken String token) {}
