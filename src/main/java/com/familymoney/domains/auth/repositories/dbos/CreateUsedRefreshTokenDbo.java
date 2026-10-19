package com.familymoney.domains.auth.repositories.dbos;

import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import java.time.Instant;

public record CreateUsedRefreshTokenDbo(RefreshToken token, TokenFamily family, Instant usedAt) {}
