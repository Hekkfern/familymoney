package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.users.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequestDto(@NotBlank @ValidEmail String email) {}
