package com.familymoney.familymoney.controllers.dtos.user;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GetMyUserResponseDto(String username, String email, Instant createdAt) {}
