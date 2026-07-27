package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.users.validation.ValidEmail;
import com.familymoney.domains.users.validation.ValidPassword;
import com.familymoney.domains.users.validation.ValidUserName;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
    @NotBlank @ValidUserName String username,
    @NotBlank @ValidEmail String email,
    @NotBlank @ValidPassword String password) {}
