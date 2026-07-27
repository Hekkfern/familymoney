package com.familymoney.security;

import com.familymoney.domains.auth.services.IAuthService;
import com.familymoney.domains.users.services.IUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final String ROLE_PREFIX = "ROLE_";

  private final JwtUtils jwtUtils;
  private final IUserService userService;
  private final IAuthService authService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    log.debug("JwtAuthFilter called for URI: {}", request.getRequestURI());
    // get authorization token from the request headers
    val token = jwtUtils.extractTokenFromHeader(request);
    if (token.isPresent()) {
      // parse the JWT token
      val tokenContentOpt = jwtUtils.parseAccessToken(token.get());
      if (tokenContentOpt.isPresent()) {
        val tokenContent = tokenContentOpt.get();
        // check if the family is blacklisted
        if (!authService.isFamilyBlacklisted(tokenContent.family())) {
          // get user id
          val userId = tokenContent.userId();
          log.debug("Authenticated user: {}", userId.value());
          // get roles from DB
          val roleOpt = userService.getUserRole(userId);
          if (roleOpt.isPresent()) {
            val role = roleOpt.get();
            log.debug("Role for user {}: {}", userId.value(), role);
            // set authorization
            val authorities = List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));
            val auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
