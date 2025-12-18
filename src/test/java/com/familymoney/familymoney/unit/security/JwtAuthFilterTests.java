package com.familymoney.familymoney.unit.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.familymoney.familymoney.security.JwtAuthFilter;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.UserId;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTests {

  @Mock private JwtUtil jwtUtil;
  @Mock private IUserService userService;
  @InjectMocks private JwtAuthFilter filter;

  @BeforeEach
  void setup() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void noAuthorizationHeader_shouldSkipAndNotAuthenticate() throws Exception {
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);

    when(request.getHeader("Authorization")).thenReturn(null);

    filter.doFilter(request, response, chain);

    // should call the chain and not set authentication
    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void nonBearerAuthorizationHeader_shouldSkipAndNotAuthenticate() throws Exception {
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);

    when(request.getHeader("Authorization")).thenReturn("Basic abcdef");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void invalidToken_shouldSkipAndNotAuthenticate() throws Exception {
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);

    // use a syntactically valid-looking token so JwtToken constructor doesn't throw
    when(request.getHeader("Authorization")).thenReturn("Bearer aaa.bbb.ccc");
    when(jwtUtil.parseAccessToken(any(JwtToken.class))).thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void validTokenButNoRole_shouldSkipAndNotAuthenticate() throws Exception {
    val uuid = UUID.randomUUID().toString();
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);
    val claims = mock(Claims.class);

    when(request.getHeader("Authorization")).thenReturn("Bearer aaa.bbb.ccc");
    when(jwtUtil.parseAccessToken(any(JwtToken.class))).thenReturn(Optional.of(claims));
    when(claims.getSubject()).thenReturn(uuid);
    when(userService.getUserRole(eq(UserId.fromString(uuid)))).thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void validTokenWithRole_shouldAuthenticateWithRoleAuthority() throws Exception {
    val uuid = UUID.randomUUID().toString();
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);
    val claims = mock(Claims.class);

    when(request.getHeader("Authorization")).thenReturn("Bearer aaa.bbb.ccc");
    when(jwtUtil.parseAccessToken(any(JwtToken.class))).thenReturn(Optional.of(claims));
    when(claims.getSubject()).thenReturn(uuid);

    val expectedUserId = UserId.fromString(uuid);
    when(userService.getUserRole(eq(expectedUserId))).thenReturn(Optional.of(Role.ADMIN));

    filter.doFilter(request, response, chain);

    // chain still called
    verify(chain).doFilter(request, response);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth, "Authentication should be set in SecurityContext");
    assertEquals(
        expectedUserId, auth.getPrincipal(), "Principal should be the UserId from token subject");
    assertTrue(
        auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + Role.ADMIN)),
        "Authorities should contain ROLE_ADMIN");
  }
}
