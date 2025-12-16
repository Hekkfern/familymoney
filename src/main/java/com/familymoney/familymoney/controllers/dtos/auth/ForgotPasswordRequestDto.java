package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.Email;
import jakarta.validation.constraints.NotNull;

public record ForgotPasswordRequestDto(@NotNull Email email) {}
