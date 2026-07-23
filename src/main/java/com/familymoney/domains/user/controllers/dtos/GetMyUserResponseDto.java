package com.familymoney.domains.user.controllers.dtos;

import java.time.Instant;

public record GetMyUserResponseDto(String username, String email, Instant createdAt) {}
