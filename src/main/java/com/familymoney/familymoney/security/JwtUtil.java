package com.familymoney.familymoney.security;

import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.JwtProperties;
import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

  private final AppProperties appProperties;
  private final JwtProperties jwtProperties;

  @NonNull
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getKey().getBytes(StandardCharsets.UTF_8));
  }

  @NonNull
  public JwtToken generateAccessToken(@NonNull UserId userId) {
    val now = Instant.now();
    val ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(15);
    val expiryDate = now.plus(ACCESS_TOKEN_VALIDITY);
    val token =
        Jwts.builder()
            .subject(userId.value().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiryDate))
            .issuer(appProperties.getName())
            .audience()
            .add(appProperties.getName())
            .and()
            .signWith(getSigningKey())
            .compact();
    return new JwtToken(token);
  }

  @NonNull
  public Optional<Claims> parseAccessToken(@NonNull JwtToken token) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(token.value())
              .getPayload();
      val isExpired = claims.getExpiration().before(new Date());
      val audienceMatches =
          claims.getAudience() != null && claims.getAudience().contains(appProperties.getName());
      val issuerMatches = claims.getIssuer().equals(appProperties.getName());
      if (!isExpired && audienceMatches && issuerMatches) {
        return Optional.of(claims);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
