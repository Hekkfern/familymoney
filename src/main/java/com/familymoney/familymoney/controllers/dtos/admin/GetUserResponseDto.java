package com.familymoney.familymoney.controllers.dtos.admin;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import java.time.Instant;

public record GetUserResponseDto(
    UserId id, UserName username, Email email, Instant createdAt, boolean isEnabled) {}
