package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.validation.ValidRefreshToken;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequestDto(@NotNull @ValidRefreshToken String refreshToken) {}
