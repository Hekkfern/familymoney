package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.RefreshToken;
import org.jspecify.annotations.NonNull;

public record TokenPair(@NonNull JwtToken accessToken, @NonNull RefreshToken refreshToken) {}
