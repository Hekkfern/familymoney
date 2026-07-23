package com.familymoney.security;

import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.properties.AppProperties;
import com.familymoney.properties.JwtProperties;
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

  /**
   * Generates a JWT access token for the given user ID and token family.
   *
   * @param userId the ID of the user for whom the token is being generated
   * @param tokenFamily the family of the token (e.g., "access", "refresh")
   * @return an AccessToken containing the generated JWT
   */
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

  /**
   * Parse and validate a JWT access token.
   *
   * <p>This method attempts to parse the provided JWT access token and perform the following
   * checks:
   *
   * <ul>
   *   <li>Verify the token signature using the configured signing key.
   *   <li>Validate standard claims: issuer and audience must match the application configuration.
   *   <li>Validate the token is not expired (expiration &gt; current time according to the
   *       configured JWT clock).
   * </ul>
   *
   * <p>If all checks pass, the method extracts the subject (interpreted as a {@link
   * com.familymoney.domains.user.types.UserId}) and the custom "family" claim (interpreted as a
   * {@link com.familymoney.domains.auth.types.TokenFamily}) and returns a {@link JwtTokenContent}
   * containing both values wrapped in {@link Optional}.
   *
   * <p>Any parsing, missing claim, or validation error will result in {@link Optional#empty()}.
   * Note that application-level checks such as token revocation/blacklist are not performed here
   * and should be implemented by the caller when required.
   *
   * @param token the access token to parse
   * @return an {@link Optional} containing the parsed {@link JwtTokenContent} when the token is
   *     valid; otherwise {@code Optional.empty()}
   */
  public Optional<JwtTokenContent> parseAccessToken(final AccessToken token) {
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
      val isExpired = claims.getExpiration().before(jwtClock.now());
      return (audienceMatches && issuerMatches && !isExpired)
          ? Optional.of(
              new JwtTokenContent(
                  UserId.fromString(claims.getSubject()),
                  TokenFamily.fromString(claims.get("family", String.class))))
          : Optional.empty();
    } catch (final Exception _) {
      return Optional.empty();
    }
  }

  public Optional<AccessToken> extractTokenFromHeader(final HttpServletRequest request) {
    val bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    log.debug("Authorization Header: {}", bearerToken);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      val rawToken = Strings.CI.removeStart(bearerToken, BEARER_PREFIX);
      log.debug("Access Token: {}", rawToken);
      try {
        return Optional.of(AccessToken.fromString(rawToken));
      } catch (final IllegalArgumentException _) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
