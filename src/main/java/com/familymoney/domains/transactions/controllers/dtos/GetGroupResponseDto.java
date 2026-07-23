package com.familymoney.domains.transactions.controllers.dtos;

import java.time.Instant;
import java.util.UUID;

public record GetGroupResponseDto(
    UUID id, String name, String description, String currency, Instant createdAt) {}
