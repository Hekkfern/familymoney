package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.user.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequestDto(@NotBlank @ValidEmail String email) {}
