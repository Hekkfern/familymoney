package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.validation.ValidEmail;
import com.familymoney.familymoney.validation.ValidPassword;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(
    @NotNull @ValidEmail String email, @NotNull @ValidPassword String password) {}
