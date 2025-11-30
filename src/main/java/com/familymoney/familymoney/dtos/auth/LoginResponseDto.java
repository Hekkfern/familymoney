package com.familymoney.familymoney.dtos.auth;

import org.springframework.lang.NonNull;

public record LoginResponseDto(@NonNull String accessToken, @NonNull String refreshToken) {}
