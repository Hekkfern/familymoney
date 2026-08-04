package com.familymoney.application;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;

import com.familymoney.application.utils.FlowUtils;
import com.familymoney.domains.auth.services.IEmailSenderService;
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

  @MockitoBean private IEmailSenderService emailSenderService;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @LocalServerPort private int port;

  // endregion

  // region Helpers

  /**
   * Registers a new user, verifies their email, and logs them in.
   *
   * @param username Name of the account
   * @param email Email address of the user account
   * @param password Password of the user account
   * @return Access Token for the logged-in user.
   */
  private String registerAndLoginUser(
      final String username, final String email, final String password) {
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair loginResponse = flowUtils.loginUser(email, password);
    return loginResponse.accessToken();
  }

  // endregion

  @BeforeEach
  void setup() {
    this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    this.flowUtils = new FlowUtils(client, emailSenderService);
  }

  @Test
  void AdminFlow_Normal_User_Tries_To_Access_Admin_Endpoints_Should_Fail() {
    // Register and login a non-admin user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final String accessToken = registerAndLoginUser(username, email, password);
    // Get data from another user
    // TODO
  }
}
