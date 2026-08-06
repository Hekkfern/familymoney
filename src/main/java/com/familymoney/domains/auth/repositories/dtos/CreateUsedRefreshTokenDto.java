package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import java.time.Instant;

public record CreateUsedRefreshTokenDto(RefreshToken token, TokenFamily family, Instant usedAt) {}
