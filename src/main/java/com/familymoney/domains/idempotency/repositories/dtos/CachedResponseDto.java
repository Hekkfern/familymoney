package com.familymoney.domains.idempotency.repositories.dtos;

public record CachedResponseDto(int httpStatus, String body) {}
