package com.familymoney.familymoney.controllers.dtos.admin;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Username;
import java.time.Instant;

public record GetUserResponseDto(
    Username username, Email email, Instant createdAt, boolean isEnabled) {}
