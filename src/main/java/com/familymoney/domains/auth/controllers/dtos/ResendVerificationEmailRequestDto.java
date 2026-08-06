package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.users.validations.ValidEmail;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequestDto(@NotBlank @ValidEmail String email) {}
