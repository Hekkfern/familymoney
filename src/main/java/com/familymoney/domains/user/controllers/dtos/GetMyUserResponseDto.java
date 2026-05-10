package com.familymoney.domains.user.controllers.dtos;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GetMyUserResponseDto(String username, String email, Instant createdAt) {}
