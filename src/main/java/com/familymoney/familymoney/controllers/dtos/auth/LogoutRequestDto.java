package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.RefreshToken;
import jakarta.validation.constraints.NotNull;

public record LogoutRequestDto(@NotNull RefreshToken refreshToken) {}
