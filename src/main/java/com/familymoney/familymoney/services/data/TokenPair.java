package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.RefreshToken;

public record TokenPair(JwtToken accessToken, RefreshToken refreshToken) {}
