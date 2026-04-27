package com.familymoney.controllers.dtos.auth;

import com.familymoney.validation.ValidEmail;
import com.familymoney.validation.ValidPassword;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(
    @NotNull @ValidEmail String email, @NotNull @ValidPassword String password) {}
