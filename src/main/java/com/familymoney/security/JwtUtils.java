package com.familymoney.security;

import com.familymoney.properties.AppProperties;
import com.familymoney.properties.JwtProperties;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.types.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
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

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final AppProperties appProperties;
  private final JwtProperties jwtProperties;
  private final io.jsonwebtoken.Clock jwtClock;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.key().getBytes(StandardCharsets.UTF_8));
  }

  public AccessToken generateAccessToken(final UserId userId, final TokenFamily tokenFamily) {
    val now = jwtClock.now();
    val expiryDate = Date.from(now.toInstant().plus(jwtProperties.accessTokenDuration()));
    val token =
        Jwts.builder()
            .subject(userId.value().toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(appProperties.name())
            .audience()
            .add(appProperties.name())
            .and()
            .claim("family", tokenFamily.toString())
            .signWith(getSigningKey())
            .compact();
    return new AccessToken(token);
  }

  public Optional<UserId> parseAccessToken(final AccessToken token) {
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
    } catch (Exception _) {
      return Optional.empty();
    }
  }

  public Optional<AccessToken> extractTokenFromHeader(final HttpServletRequest request) {
    val bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    log.debug("Authorization Header: {}", bearerToken);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      val rawToken = Strings.CI.removeStart(bearerToken, BEARER_PREFIX);
      log.debug("Access Token: {}", rawToken);
      return Optional.of(AccessToken.fromString(rawToken));
    }
    return Optional.empty();
  }
}
