package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.auth.validation.ValidRefreshToken;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(@NotBlank @ValidRefreshToken String refreshToken) {}
