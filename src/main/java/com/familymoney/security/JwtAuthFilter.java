package com.familymoney.security;

import com.familymoney.domains.auth.services.IAuthService;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.users.services.IUserService;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    final Optional<AccessToken> token = jwtUtils.extractTokenFromHeader(request);
    if (token.isPresent()) {
      // parse the JWT token
      final Optional<JwtTokenContent> tokenContentOpt = jwtUtils.parseAccessToken(token.get());
      if (tokenContentOpt.isPresent()) {
        final JwtTokenContent tokenContent = tokenContentOpt.get();
        // check if the family is blacklisted
        if (!authService.isFamilyBlacklisted(tokenContent.family())) {
          // get user id
          final UserId userId = tokenContent.userId();
          final Optional<UserData> userDataOpt = userService.getUserData(userId);
          if (userDataOpt.isPresent() && userDataOpt.get().isEnabled()) {
            log.debug("Authenticated user: {}", userId.value());
            // get roles from DB
            final Optional<Role> roleOpt = userService.getUserRole(userId);
            if (roleOpt.isPresent()) {
              final Role role = roleOpt.get();
              log.debug("Role for user {}: {}", userId.value(), role);
              // set authorization
              final List<SimpleGrantedAuthority> authorities =
                  List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));
              final UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(userId, null, authorities);
              auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(auth);
            }
          }
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
