package com.familymoney.familymoney.controllers.dtos.auth;

import lombok.Builder;

@Builder
public record RefreshResponseDto(String accessToken, String refreshToken) {}
