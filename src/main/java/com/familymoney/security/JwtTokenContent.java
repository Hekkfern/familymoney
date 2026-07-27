package com.familymoney.security;

import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.UserId;

public record JwtTokenContent(UserId userId, TokenFamily family) {}
