package com.familymoney.controllers.dtos.grouptransaction;

import lombok.Builder;

@Builder
public record GetInvitationTokenResponseDto(String token) {}
