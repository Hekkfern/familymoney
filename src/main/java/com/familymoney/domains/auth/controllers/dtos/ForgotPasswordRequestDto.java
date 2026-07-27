package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.users.validation.ValidEmail;
import jakarta.validation.constraints.NotNull;

public record ForgotPasswordRequestDto(@NotNull @ValidEmail String email) {}
