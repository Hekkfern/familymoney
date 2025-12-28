package com.familymoney.familymoney.unit.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.JwtProperties;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.UserId;
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
public class JwtUtilTests {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Spy private AppProperties appProperties = new AppProperties("testapp");
  @Mock private io.jsonwebtoken.Clock jwtClock;

  @Spy
  private JwtProperties jwtProperties =
      new JwtProperties("qVvdxJtduRDiRyjfz2HnPLs12314kG6HxEfHkV1LjbBBAuVjJsNvgrlWu18W3GEj12");

  @InjectMocks private JwtUtil jwtUtil;

  @Test
  void parse_invalid_token_fails() {
    val token = JwtToken.fromString("aaa.bbbb.ccc");
    val claimsOpt = jwtUtil.parseAccessToken(token);
    assertTrue(claimsOpt.isEmpty());
  }

  @Test
  void parse_expired_token_fails() {
    when(jwtClock.now())
        .thenReturn(Date.from(now))
        .thenReturn(Date.from(now.plus(10, ChronoUnit.DAYS)));

    val token = jwtUtil.generateAccessToken(UserId.fromUuid(UUID.randomUUID()));
    val claimsOpt = jwtUtil.parseAccessToken(token);
    assertTrue(claimsOpt.isEmpty());
  }

  @Test
  void parse_valid_token_succeeds() {
    when(jwtClock.now()).thenReturn(Date.from(now));

    val token = jwtUtil.generateAccessToken(UserId.fromUuid(UUID.randomUUID()));
    val claimsOpt = jwtUtil.parseAccessToken(token);
    assertTrue(claimsOpt.isPresent());
    val claims = claimsOpt.get();
    assertEquals("testapp", claims.getIssuer());
    assertEquals(1, claims.getAudience().size());
    assertTrue(claims.getAudience().contains("testapp"));
  }
}
