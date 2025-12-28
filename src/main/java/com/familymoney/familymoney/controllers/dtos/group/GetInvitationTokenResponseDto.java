package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.validation.ValidShareGroupToken;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GetInvitationTokenResponseDto(@NotNull @ValidShareGroupToken String token) {}
