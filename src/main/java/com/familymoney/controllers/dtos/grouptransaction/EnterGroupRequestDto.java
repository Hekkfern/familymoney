package com.familymoney.controllers.dtos.grouptransaction;

import com.familymoney.validation.ValidShareGroupToken;
import jakarta.validation.constraints.NotNull;

public record EnterGroupRequestDto(@NotNull @ValidShareGroupToken String token) {}
