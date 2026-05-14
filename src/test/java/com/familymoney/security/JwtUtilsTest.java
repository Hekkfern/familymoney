package com.familymoney.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.properties.AppProperties;
import com.familymoney.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtUtilsTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Spy private AppProperties appProperties = new AppProperties("testapp");
  @Mock private io.jsonwebtoken.Clock jwtClock;

  @Spy
  private JwtProperties jwtProperties =
      new JwtProperties(
          "qVvdxJtduRDiRyjfz2HnPLs12314kG6HxEfHkV1LjbBBAuVjJsNvgrlWu18W3GEj12",
          Duration.ofMinutes(5),
          Duration.ofHours(24));

  @InjectMocks private JwtUtils jwtUtils;

  // region JwtUtils.generateAccessToken()

  // TODO

  // endregion

  // region JwtUtils.parseAccessToken()

  @Test
  void parseAccessToken_invalid_token_fails() {
    val token = AccessToken.fromString("aaa.bbbb.ccc");

    val contentOpt = jwtUtils.parseAccessToken(token);

    assertThat(contentOpt).isEmpty();
  }

  @Test
  void parseAccessToken_expired_token_fails() {
    when(jwtClock.now())
        .thenReturn(Date.from(now))
        .thenReturn(Date.from(now.plus(10, ChronoUnit.DAYS)));

    val userId = UserId.generate();
    val family = TokenFamily.generate();
    val token = jwtUtils.generateAccessToken(userId, family);

    val contentOpt = jwtUtils.parseAccessToken(token);
    assertThat(contentOpt).isEmpty();
  }

  @Test
  void parseAccessToken_valid_token_succeeds() {
    when(jwtClock.now()).thenReturn(Date.from(now));

    val userId = UserId.generate();
    val family = TokenFamily.generate();
    val token = jwtUtils.generateAccessToken(userId, family);

    val contentOpt = jwtUtils.parseAccessToken(token);
    assertThat(contentOpt).isNotEmpty();
    val content = contentOpt.get();
    assertThat(content).isNotNull();
    assertThat(content.userId()).isEqualTo(userId);
    assertThat(content.family()).isEqualTo(family);
  }

  // endregion

  // region JwtUtils.extractTokenFromHeader()

  @Test
  void extractTokenFromHeader_without_authorization_header_returns_empty() {
    val request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn(null);

    val tokenOpt = jwtUtils.extractTokenFromHeader(request);
    assertThat(tokenOpt).isEmpty();
  }

  @Test
  void extractTokenFromHeader_without_bearer_authorization_header_returns_empty() {
    val request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Basic aaaaaaaa");

    val tokenOpt = jwtUtils.extractTokenFromHeader(request);
    assertThat(tokenOpt).isEmpty();
  }

  @Test
  void extractTokenFromHeader_with_bearer_authorization_header_returns_token() {
    val request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Bearer aaaa.bbbb.cccc");

    val tokenOpt = jwtUtils.extractTokenFromHeader(request);
    assertThat(tokenOpt).isNotEmpty().contains(AccessToken.fromString("aaaa.bbbb.cccc"));
  }

  // endregion
}
