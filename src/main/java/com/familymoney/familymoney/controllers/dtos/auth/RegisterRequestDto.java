package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.validation.ValidEmail;
import com.familymoney.familymoney.validation.ValidPassword;
import com.familymoney.familymoney.validation.ValidUserName;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDto(
    @NotNull @ValidUserName String username,
    @NotNull @ValidEmail String email,
    @NotNull @ValidPassword String password) {}
