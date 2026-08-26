package com.familymoney.flows;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.users.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.flows.utils.FlowUtils;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.UserControllerUriFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserFlowTest {

  // region Fields

  private RestTestClient client;
  private FlowUtils flowUtils;
  @MockitoBean private IEmailSenderService emailSenderService;

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
  void get_user_data_when_logged_in_returns_data() {
    // Register and login user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final FlowUtils.TokenPair tokenPair = flowUtils.registerAndLoginUser(username, email, password);
    // Get user data
    final GetMyUserResponseDto userDataResponse =
        client
            .get()
            .uri(UserControllerUriFactory.getMePath())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GetMyUserResponseDto.class)
            .returnResult()
            .getResponseBody();
    assertEquals(username, userDataResponse.username());
    assertEquals(email, userDataResponse.email());
  }

  @Test
  void get_user_data_when_not_logged_in_returns_unauthorized() {
    // Get user data without logging in
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void get_user_data_when_logged_out_but_reusing_access_token_returns_unauthorized() {
    // Register and login user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final FlowUtils.TokenPair tokenPair = flowUtils.registerAndLoginUser(username, email, password);
    // Logout user
    flowUtils.logoutUser(tokenPair.refreshToken());
    // Get user data with the same access token after logout
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void update_user_data_when_logged_in_returns_success() {
    // Register and login user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final FlowUtils.TokenPair tokenPair = flowUtils.registerAndLoginUser(username, email, password);
    // Update user data
    final String newUsername = FakeGenerator.username();
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
        .body(UpdateUserRequestDto.builder().username(newUsername).build())
        .exchange()
        .expectStatus()
        .isOk();
    // Get user data to verify update
    final GetMyUserResponseDto userDataResponse =
        client
            .get()
            .uri(UserControllerUriFactory.getMePath())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GetMyUserResponseDto.class)
            .returnResult()
            .getResponseBody();
    assertEquals(newUsername, userDataResponse.username());
  }

  @Test
  void update_user_data_when_not_logged_in_returns_unauthorized() {
    // Update user data without logging in
    final String newUsername = FakeGenerator.username();
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .body(UpdateUserRequestDto.builder().username(newUsername).build())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void delete_user_when_logged_in_returns_success() {
    // Register and login user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final FlowUtils.TokenPair tokenPair = flowUtils.registerAndLoginUser(username, email, password);
    // Delete user
    client
        .delete()
        .uri(UserControllerUriFactory.getMePath())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
        .exchange()
        .expectStatus()
        .isOk();
    // Get user data to verify deletion
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
        .exchange()
        .expectStatus()
        .isUnauthorized();
    // Try to log in again to verify deletion
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(email, password))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void delete_user_when_not_logged_in_returns_unauthorized() {
    // Delete user without logging in
    client
        .delete()
        .uri(UserControllerUriFactory.getMePath())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
