package com.familymoney.familymoney.config;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SecurityConfigIntegrationTest {

  private RestTestClient client;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @LocalServerPort private int port;

  @RestController
  @RequestMapping("/api/v1/admin")
  static class AdminTestController {
    @GetMapping("/ping")
    public String ping() {
      return "pong";
    }
  }

  @BeforeEach
  void setup() {
    client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminEndpoint_allowsAdminRole() {
    client.get().uri("/api/v1/admin/ping").exchange().expectStatus().isOk();
  }

  @Test
  @WithMockUser(roles = "USER")
  void adminEndpoint_forbidsUserRole() {}

  @Test
  void adminEndpoint_returnsUnauthorizedWhenUnauthenticated() {}
}
