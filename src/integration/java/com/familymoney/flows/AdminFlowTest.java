package com.familymoney.flows;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;

import com.familymoney.domains.auth.services.EmailSenderService;
import com.familymoney.flows.utils.FlowUtils;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AdminFlowTest {

  // region Fields

  private RestTestClient client;
  private FlowUtils flowUtils;

  @MockitoBean private EmailSenderService emailSenderService;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @LocalServerPort private int port;

  // endregion

  @BeforeEach
  void setup() {
    this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    this.flowUtils = new FlowUtils(client, emailSenderService);
  }

  @Test
  void normal_user_accessing_admin_endpoints_returns_unauthorized() {
    // Register and login a non-admin user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final FlowUtils.TokenPair tokenPair = flowUtils.registerAndLoginUser(username, email, password);
    // Get data from another user
    // TODO
  }
}
