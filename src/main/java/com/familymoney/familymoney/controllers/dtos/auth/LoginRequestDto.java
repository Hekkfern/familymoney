package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(@NotNull Email email, @NotNull Password password) {}
