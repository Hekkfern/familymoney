package com.familymoney.familymoney.dtos.auth;

import com.familymoney.familymoney.validation.RefreshToken;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(@NotBlank @RefreshToken String refreshToken) {}
