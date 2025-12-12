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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

  private final AppProperties appProperties;
  private final JwtProperties jwtProperties;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.key().getBytes(StandardCharsets.UTF_8));
  }

  public JwtToken generateAccessToken(UserId userId) {
    val now = Instant.now();
    val ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(15);
    val expiryDate = now.plus(ACCESS_TOKEN_VALIDITY);
    val token =
        Jwts.builder()
            .subject(userId.value().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiryDate))
            .issuer(appProperties.name())
            .audience()
            .add(appProperties.name())
            .and()
            .signWith(getSigningKey())
            .compact();
    return new JwtToken(token);
  }

  public Optional<Claims> parseAccessToken(JwtToken token) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(token.value())
              .getPayload();
      val isExpired = claims.getExpiration().before(new Date());
      val audienceMatches =
          claims.getAudience() != null && claims.getAudience().contains(appProperties.name());
      val issuerMatches = claims.getIssuer().equals(appProperties.name());
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
