package com.familymoney.familymoney.controllers.dtos.admin;

import java.time.Instant;

public record GetUserResponseDto(
    String username, String email, Instant createdAt, boolean isEnabled) {}
