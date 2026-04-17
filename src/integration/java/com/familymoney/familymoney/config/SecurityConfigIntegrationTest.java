package com.familymoney.familymoney.config;

import com.familymoney.familymoney.security.JwtAuthFilter;
import com.familymoney.familymoney.security.JwtUtils;
import com.familymoney.familymoney.services.IUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigIntegrationTest.TestConfig.AdminTestController.class)
@AutoConfigureRestTestClient
class SecurityConfigIntegrationTest {

  @Autowired private RestTestClient client;

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    JwtAuthFilter jwtAuthFilter() {
      return new JwtAuthFilter(
          Mockito.mock(JwtUtils.class),
          Mockito.mock(IUserService.class)) { // depends on constructor
        @Override
        protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
          chain.doFilter(req, res);
        }
      };
    }

    @RestController
    @RequestMapping("admin")
    static class AdminTestController {

      @GetMapping(path = "ping", version = "1")
      public String ping() {
        return "pong";
      }
    }
  }

  @Test
  @WithMockUser(username = "123e1450-39dd-11f1-9992-5dbb91c933de", roles = "ADMIN")
  void adminEndpoint_allowsAdminRole() {
    client.get().uri("/api/v1/admin/ping").exchange().expectStatus().isOk();
  }

  @Test
  @WithMockUser(username = "123e1450-39dd-11f1-9992-5dbb91c933de", roles = "USER")
  void adminEndpoint_forbidsUserRole() {
    client.get().uri("/api/v1/admin/ping").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void adminEndpoint_returnsUnauthorizedWhenUnauthenticated() {
    client.get().uri("/api/v1/admin/ping").exchange().expectStatus().isUnauthorized();
  }
}
