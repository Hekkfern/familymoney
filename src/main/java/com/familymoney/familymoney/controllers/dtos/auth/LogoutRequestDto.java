package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.validation.RefreshToken;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(@NotBlank @RefreshToken String refreshToken) {}
