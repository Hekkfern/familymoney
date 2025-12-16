package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.Username;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDto(
    @NotNull Username username, @NotNull Email email, @NotNull Password password) {}
