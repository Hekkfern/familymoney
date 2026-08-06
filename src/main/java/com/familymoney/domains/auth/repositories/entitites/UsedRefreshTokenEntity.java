package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.TokenFamily;
import java.time.Instant;

public record UsedRefreshTokenEntity(TokenFamily family, Instant usedAt, Instant createdAt) {}
