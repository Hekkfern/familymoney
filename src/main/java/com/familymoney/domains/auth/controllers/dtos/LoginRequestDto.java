package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.users.validations.ValidEmail;
import com.familymoney.domains.users.validations.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank @ValidEmail String email, @NotBlank @ValidPassword String password) {}
