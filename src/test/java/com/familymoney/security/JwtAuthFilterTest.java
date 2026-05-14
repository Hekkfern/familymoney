package com.familymoney.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.familymoney.domains.auth.services.IAuthService;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
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
class JwtAuthFilterTest {

  @Mock private JwtUtils jwtUtils;
  @Mock private IUserService userService;
  @Mock private IAuthService authService;
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

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    // should call the chain and not set authentication
    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void invalidToken_shouldSkipAndNotAuthenticate() throws Exception {
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);

    // use a syntactically valid-looking token so JwtToken constructor doesn't throw
    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString("aaa.bbb.ccc")));
    when(jwtUtils.parseAccessToken(any(AccessToken.class))).thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void validTokenButFamilyBlacklisted_shouldSkipAndNotAuthenticate() throws Exception {
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString("aaa.bbb.ccc")));
    when(jwtUtils.parseAccessToken(any(AccessToken.class)))
        .thenReturn(Optional.of(new JwtTokenContent(userId, family)));
    when(authService.isFamilyBlacklisted(any())).thenReturn(true);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void validTokenButNoRole_shouldSkipAndNotAuthenticate() throws Exception {
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString("aaa.bbb.ccc")));
    when(jwtUtils.parseAccessToken(any(AccessToken.class)))
        .thenReturn(Optional.of(new JwtTokenContent(userId, family)));
    when(authService.isFamilyBlacklisted(any())).thenReturn(false);
    when(userService.getUserRole(userId)).thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void validTokenWithRole_shouldAuthenticateWithRoleAuthority() throws Exception {
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    val request = mock(HttpServletRequest.class);
    val response = mock(HttpServletResponse.class);
    val chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString("aaa.bbb.ccc")));
    when(jwtUtils.parseAccessToken(any(AccessToken.class)))
        .thenReturn(Optional.of(new JwtTokenContent(userId, family)));
    when(authService.isFamilyBlacklisted(any())).thenReturn(false);
    when(userService.getUserRole(userId)).thenReturn(Optional.of(Role.ADMIN));

    filter.doFilter(request, response, chain);

    // chain still called
    verify(chain).doFilter(request, response);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth, "Authentication should be set in SecurityContext");
    assertEquals(userId, auth.getPrincipal(), "Principal should be the UserId from token subject");
    assertTrue(
        auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + Role.ADMIN)),
        "Authorities should contain ROLE_ADMIN");
  }
}
