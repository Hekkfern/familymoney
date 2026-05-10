package com.familymoney.domains.auth.controllers.dtos;

import lombok.Builder;

@Builder
public record LoginResponseDto(String accessToken, String refreshToken) {}
