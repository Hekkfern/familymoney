package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.auth.validation.ValidRefreshToken;
import jakarta.validation.constraints.NotNull;

public record LogoutRequestDto(@NotNull @ValidRefreshToken String refreshToken) {}
