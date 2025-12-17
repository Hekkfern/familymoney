package com.familymoney.familymoney.security;

import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ROLE_PREFIX = "ROLE_";

  private final JwtUtil jwtUtil;
  private final IUserService userService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // get authorization token from the request headers
    val authHeader = request.getHeader(AUTHORIZATION_HEADER);
    String rawToken = null;
    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      rawToken = Strings.CI.removeStart(authHeader, BEARER_PREFIX);
    }
    if (rawToken == null) {
      // skip
      filterChain.doFilter(request, response);
      return;
    }
    // parse the JWT token
    val accessTokenOpt = jwtUtil.parseAccessToken(new JwtToken(rawToken));
    if (accessTokenOpt.isEmpty()) {
      // skip
      filterChain.doFilter(request, response);
      return;
    }
    val accessToken = accessTokenOpt.get();
    val userId = UserId.fromString(accessToken.getSubject());
    // get roles from DB
    val roleOpt = userService.getUserRole(userId);
    if (roleOpt.isEmpty()) {
      // skip
      filterChain.doFilter(request, response);
      return;
    }
    // set authorization
    val authorities = List.of(new SimpleGrantedAuthority(ROLE_PREFIX + roleOpt));
    val auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(auth);
    // finish
    filterChain.doFilter(request, response);
  }
}
