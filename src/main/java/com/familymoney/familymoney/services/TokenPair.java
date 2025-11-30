package com.familymoney.familymoney.services;

import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.RefreshToken;

public record TokenPair(JwtToken accessToken, RefreshToken refreshToken) {}
