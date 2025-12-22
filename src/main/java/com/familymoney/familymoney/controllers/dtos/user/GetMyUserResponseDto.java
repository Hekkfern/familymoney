package com.familymoney.familymoney.controllers.dtos.user;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserName;
import java.time.Instant;

public record GetMyUserResponseDto(UserName username, Email email, Instant createdAt) {}
