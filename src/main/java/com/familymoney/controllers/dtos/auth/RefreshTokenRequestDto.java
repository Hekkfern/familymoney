package com.familymoney.controllers.dtos.auth;

import com.familymoney.validation.ValidRefreshToken;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequestDto(@NotNull @ValidRefreshToken String refreshToken) {}
