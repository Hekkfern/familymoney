package com.familymoney.familymoney.controllers.dtos.auth;

import org.jspecify.annotations.NonNull;

public record LoginResponseDto(@NonNull String accessToken, @NonNull String refreshToken) {}
