package com.familymoney.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.domains.auth.services.AuthService;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Optional;
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

  private static final String VALID_JWT_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

  @Mock private JwtUtils jwtUtils;
  @Mock private UserService userService;
  @Mock private AuthService authService;
  @InjectMocks private JwtAuthFilter filter;

  @BeforeEach
  void setup() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private UserData enabledUser(final UserId userId) {
    return new UserData(
        userId,
        UserName.fromString("enabled-user"),
        Email.fromString("enabled@example.com"),
        Instant.EPOCH,
        true,
        true);
  }

  @Test
  void skip_and_no_authenticate_when_invalid_token_in_header() throws Exception {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final FilterChain chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    // should call the chain and not set authentication
    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void skip_and_no_authenticate_when_valid_Token_in_header_but_family_is_blacklisted()
      throws Exception {
    final UserId userId = UserId.generate();
    final TokenFamily family = TokenFamily.generate();
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final FilterChain chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString(VALID_JWT_TOKEN)));
    when(jwtUtils.parseAccessToken(any(AccessToken.class)))
        .thenReturn(Optional.of(new JwtTokenContent(userId, family)));
    when(authService.isFamilyBlacklisted(any())).thenReturn(true);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void skip_and_no_authenticate_when_valid_Token_in_header_but_no_role() throws Exception {
    final UserId userId = UserId.generate();
    final TokenFamily family = TokenFamily.generate();
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final FilterChain chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString(VALID_JWT_TOKEN)));
    when(jwtUtils.parseAccessToken(any(AccessToken.class)))
        .thenReturn(Optional.of(new JwtTokenContent(userId, family)));
    when(authService.isFamilyBlacklisted(any())).thenReturn(false);
    when(userService.getUserData(userId)).thenReturn(Optional.of(enabledUser(userId)));
    when(userService.getUserRole(userId)).thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void skip_and_no_authenticate_when_valid_token_belongs_to_disabled_user() throws Exception {
    final UserId userId = UserId.generate();
    final TokenFamily family = TokenFamily.generate();
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final FilterChain chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString(VALID_JWT_TOKEN)));
    when(jwtUtils.parseAccessToken(any(AccessToken.class)))
        .thenReturn(Optional.of(new JwtTokenContent(userId, family)));
    when(authService.isFamilyBlacklisted(any())).thenReturn(false);
    when(userService.getUserData(userId))
        .thenReturn(
            Optional.of(
                new UserData(
                    userId,
                    UserName.fromString("disabled-user"),
                    Email.fromString("disabled@example.com"),
                    Instant.EPOCH,
                    true,
                    false)));

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void authenticate_when_valid_token_in_header_and_role_is_present() throws Exception {
    final UserId userId = UserId.generate();
    final TokenFamily family = TokenFamily.generate();
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final FilterChain chain = mock(FilterChain.class);

    when(jwtUtils.extractTokenFromHeader(any(HttpServletRequest.class)))
        .thenReturn(Optional.of(AccessToken.fromString(VALID_JWT_TOKEN)));
    when(jwtUtils.parseAccessToken(any(AccessToken.class)))
        .thenReturn(Optional.of(new JwtTokenContent(userId, family)));
    when(authService.isFamilyBlacklisted(any())).thenReturn(false);
    when(userService.getUserData(userId)).thenReturn(Optional.of(enabledUser(userId)));
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
