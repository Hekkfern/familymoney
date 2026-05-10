package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.user.validation.ValidEmail;
import com.familymoney.domains.user.validation.ValidPassword;
import com.familymoney.domains.user.validation.ValidUserName;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDto(
    @NotNull @ValidUserName String username,
    @NotNull @ValidEmail String email,
    @NotNull @ValidPassword String password) {}
