package com.familymoney.familymoney.unit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.JwtProperties;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.types.JwtToken;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTests {

  @Spy private AppProperties appProperties = new AppProperties("testapp");

  @Spy
  private JwtProperties jwtProperties =
      new JwtProperties("qVvdxJtduRDiRyjfz2HnPLs12314kG6HxEfHkV1LjbBBAuVjJsNvgrlWu18W3GEj12");

  @InjectMocks private JwtUtil jwtUtil;

  @Test
  void parseInvalidToken() {
    val token = JwtToken.fromString("aaa.bbbb.ccc");
    assertEquals(Optional.empty(), jwtUtil.parseAccessToken(token));
  }

  @Test
  void parseValidToken() {
    val token = JwtToken.fromString("aaa.bbbb.ccc");
    val claimsOpt = jwtUtil.parseAccessToken(token);
    assertNotEquals(Optional.empty(), claimsOpt);
    val claims = claimsOpt.get();
    assertEquals("testapp", claims.getIssuer());
  }
}
