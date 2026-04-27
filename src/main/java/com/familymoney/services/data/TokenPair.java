package com.familymoney.services.data;

import com.familymoney.types.JwtToken;
import com.familymoney.types.RefreshToken;

public record TokenPair(JwtToken accessToken, RefreshToken refreshToken) {}
