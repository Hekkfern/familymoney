package com.familymoney.familymoney.security;

import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.JwtProperties;
import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

  private final Duration ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(15);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final AppProperties appProperties;
  private final JwtProperties jwtProperties;
  private final io.jsonwebtoken.Clock jwtClock;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.key().getBytes(StandardCharsets.UTF_8));
  }

  public JwtToken generateAccessToken(UserId userId) {
    val now = jwtClock.now();
    val expiryDate = Date.from(now.toInstant().plus(ACCESS_TOKEN_VALIDITY));
    val token =
        Jwts.builder()
            .subject(userId.value().toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(appProperties.name())
            .audience()
            .add(appProperties.name())
            .and()
            .signWith(getSigningKey())
            .compact();
    return new JwtToken(token);
  }

  public Optional<UserId> parseAccessToken(JwtToken token) {
    try {
      val claims =
          Jwts.parser()
              .clock(jwtClock)
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(token.value())
              .getPayload();
      val audienceMatches =
          claims.getAudience() != null && claims.getAudience().contains(appProperties.name());
      val issuerMatches = claims.getIssuer().equals(appProperties.name());
      return (audienceMatches && issuerMatches)
          ? Optional.of(UserId.fromString(claims.getSubject()))
          : Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<JwtToken> extractTokenFromHeader(HttpServletRequest request) {
    val bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    log.debug("Authorization Header: {}", bearerToken);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      val rawToken = Strings.CI.removeStart(bearerToken, BEARER_PREFIX);
      log.debug("Access Token: {}", rawToken);
      return Optional.of(JwtToken.fromString(rawToken));
    }
    return Optional.empty();
  }
}
