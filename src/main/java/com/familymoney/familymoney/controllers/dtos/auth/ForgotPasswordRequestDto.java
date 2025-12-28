package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.validation.ValidEmail;
import jakarta.validation.constraints.NotNull;

public record ForgotPasswordRequestDto(@NotNull @ValidEmail String email) {}
