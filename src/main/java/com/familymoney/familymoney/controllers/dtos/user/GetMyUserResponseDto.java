package com.familymoney.familymoney.controllers.dtos.user;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Username;
import java.time.Instant;

public record GetMyUserResponseDto(Username username, Email email, Instant createdAt) {}
