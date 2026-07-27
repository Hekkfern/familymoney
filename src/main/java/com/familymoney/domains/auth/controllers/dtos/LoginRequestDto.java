package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.users.validation.ValidEmail;
import com.familymoney.domains.users.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank @ValidEmail String email, @NotBlank @ValidPassword String password) {}
