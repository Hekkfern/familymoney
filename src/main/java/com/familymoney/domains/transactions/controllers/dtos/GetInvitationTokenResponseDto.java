package com.familymoney.domains.transactions.controllers.dtos;

import lombok.Builder;

@Builder
public record GetInvitationTokenResponseDto(String token) {}
