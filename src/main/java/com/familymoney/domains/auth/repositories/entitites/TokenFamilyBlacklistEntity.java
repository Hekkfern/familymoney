package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.TokenFamily;
import java.time.Instant;
import lombok.Builder;

@Builder
public record TokenFamilyBlacklistEntity(TokenFamily family, Instant createdAt) {}
