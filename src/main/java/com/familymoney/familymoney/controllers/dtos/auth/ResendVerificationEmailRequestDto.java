package com.familymoney.familymoney.controllers.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequestDto(@NotBlank @Email String email) {}
