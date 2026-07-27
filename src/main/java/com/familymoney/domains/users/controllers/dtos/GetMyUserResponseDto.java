package com.familymoney.domains.users.controllers.dtos;

import java.time.Instant;

public record GetMyUserResponseDto(String username, String email, Instant createdAt) {}
