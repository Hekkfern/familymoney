package com.familymoney.domains.auth.services.data;

import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.RefreshToken;

public record TokenPair(AccessToken accessToken, RefreshToken refreshToken) {}
