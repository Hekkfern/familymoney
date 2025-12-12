package com.familymoney.familymoney.controllers.dtos.user;

import java.time.Instant;

public record GetMyUserResponseDto(String username, String email, Instant createdAt) {}
