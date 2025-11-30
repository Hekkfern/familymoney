package com.familymoney.familymoney.dtos.auth;

import org.springframework.lang.NonNull;

public record RefreshResponseDto(@NonNull String accessToken, @NonNull String refreshToken) {}
