package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.user.validation.ValidEmail;
import com.familymoney.domains.user.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank @ValidEmail String email, @NotBlank @ValidPassword String password) {}
