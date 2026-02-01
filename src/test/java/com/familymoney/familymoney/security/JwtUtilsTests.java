package com.familymoney.familymoney.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.JwtProperties;
import com.familymoney.familymoney.security.JwtUtils;
import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.UserId;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
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
public class JwtUtilsTests {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Spy private AppProperties appProperties = new AppProperties("testapp");
  @Mock private io.jsonwebtoken.Clock jwtClock;

  @Spy
  private JwtProperties jwtProperties =
      new JwtProperties("qVvdxJtduRDiRyjfz2HnPLs12314kG6HxEfHkV1LjbBBAuVjJsNvgrlWu18W3GEj12");

  @InjectMocks private JwtUtils jwtUtils;

  // region parseAccessToken

  @Test
  void parse_invalid_token_fails() {
    val token = JwtToken.fromString("aaa.bbbb.ccc");
    val userIdOpt = jwtUtils.parseAccessToken(token);
    assertThat(userIdOpt).isEmpty();
  }

  @Test
  void parse_expired_token_fails() {
    when(jwtClock.now())
        .thenReturn(Date.from(now))
        .thenReturn(Date.from(now.plus(10, ChronoUnit.DAYS)));

    val token = jwtUtils.generateAccessToken(UserId.fromUuid(UUID.randomUUID()));
    val userIdOpt = jwtUtils.parseAccessToken(token);
    assertThat(userIdOpt).isEmpty();
  }

  @Test
  void parse_valid_token_succeeds() {
    val userid = UserId.fromUuid(UUID.randomUUID());
    when(jwtClock.now()).thenReturn(Date.from(now));

    val token = jwtUtils.generateAccessToken(userid);
    val userIdOpt = jwtUtils.parseAccessToken(token);
    assertThat(userIdOpt).isNotEmpty().contains(userid);
  }

  // endregion

  // region extractTokenFromHeader

  @Test
  void extract_token_from_request_without_authorization_header_returns_empty() {
    val request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn(null);

    val tokenOpt = jwtUtils.extractTokenFromHeader(request);
    assertThat(tokenOpt).isEmpty();
  }

  @Test
  void extract_token_from_request_without_bearer_authorization_header_returns_empty() {
    val request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Basic aaaaaaaa");

    val tokenOpt = jwtUtils.extractTokenFromHeader(request);
    assertThat(tokenOpt).isEmpty();
  }

  @Test
  void extract_token_from_request_with_bearer_authorization_header_returns_token() {
    val request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Bearer aaaa.bbbb.cccc");

    val tokenOpt = jwtUtils.extractTokenFromHeader(request);
    assertThat(tokenOpt).isNotEmpty().contains(JwtToken.fromString("aaaa.bbbb.cccc"));
  }

  // endregion
}
