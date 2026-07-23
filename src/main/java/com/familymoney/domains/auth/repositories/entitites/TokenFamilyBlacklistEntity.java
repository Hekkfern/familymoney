package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.TokenFamily;
import java.time.Instant;

public record TokenFamilyBlacklistEntity(TokenFamily family, Instant createdAt) {}
