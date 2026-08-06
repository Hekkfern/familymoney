package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.users.validations.ValidEmail;
import com.familymoney.domains.users.validations.ValidPassword;
import com.familymoney.domains.users.validations.ValidUserName;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
    @NotBlank @ValidUserName String username,
    @NotBlank @ValidEmail String email,
    @NotBlank @ValidPassword String password) {}
