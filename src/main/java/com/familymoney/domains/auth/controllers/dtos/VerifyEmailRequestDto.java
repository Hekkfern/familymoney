package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.auth.validations.ValidEmailVerificationToken;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequestDto(@NotBlank @ValidEmailVerificationToken String token) {}
