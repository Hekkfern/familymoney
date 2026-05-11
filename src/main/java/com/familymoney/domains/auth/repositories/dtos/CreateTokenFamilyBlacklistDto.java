package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.TokenFamily;
import java.time.Instant;
import lombok.Builder;

@Builder
public record CreateTokenFamilyBlacklistDto(TokenFamily family, Instant createdAt) {}
