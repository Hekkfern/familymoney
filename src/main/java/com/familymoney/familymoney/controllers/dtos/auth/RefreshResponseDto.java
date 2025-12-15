package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.RefreshToken;

public record RefreshResponseDto(JwtToken accessToken, RefreshToken refreshToken) {}
