package com.familymoney.domains.auth.controllers.dtos;

import lombok.Builder;

@Builder
public record RefreshResponseDto(String accessToken, String refreshToken) {}
