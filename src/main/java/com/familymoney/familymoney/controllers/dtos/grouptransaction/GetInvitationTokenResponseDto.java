package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import lombok.Builder;

@Builder
public record GetInvitationTokenResponseDto(String token) {}
