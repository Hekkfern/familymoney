package com.familymoney.controllers.dtos.auth;

import lombok.Builder;

@Builder
public record LoginResponseDto(String accessToken, String refreshToken) {}
