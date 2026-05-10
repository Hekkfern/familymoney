package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.user.validation.ValidEmail;
import com.familymoney.domains.user.validation.ValidPassword;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(
    @NotNull @ValidEmail String email, @NotNull @ValidPassword String password) {}
