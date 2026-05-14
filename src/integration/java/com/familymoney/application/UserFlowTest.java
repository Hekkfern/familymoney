package com.familymoney.application;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LoginResponseDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.user.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.UserControllerUriFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserFlowTest {

  // region Fields

  private RestTestClient client;

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
  private String registerAndLoginUser(String username, String email, String password) {
    // Mock email sender
    val verificationTokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);

    // register the new user
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username, email, password))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();

    // get the captured email verification token
    verify(emailSenderService)
        .sendEmailVerificationEmail(any(), any(), verificationTokenCaptor.capture());
    val verificationToken = verificationTokenCaptor.getValue();

    // verify email
    client
        .get()
        .uri(AuthControllerUriFactory.getVerifyEmailPath(verificationToken.value()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();

    val loginResponse =
        client
            .post()
            .uri(AuthControllerUriFactory.getLoginPath())
            .body(new LoginRequestDto(email, password))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LoginResponseDto.class)
            .returnResult()
            .getResponseBody();
    return loginResponse.accessToken().toString();
  }

  // endregion

  @BeforeEach
  void setup() {
    client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void UserFlow_Get_user_data_when_logged_in() {
    // Register and login user
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    val accessToken = registerAndLoginUser(username, email, password);
    // Get user data
    val userDataResponse =
        client
            .get()
            .uri(UserControllerUriFactory.getMePath())
            .header("Authorization", "Bearer " + accessToken)
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
  void UserFlow_Get_user_data_when_not_logged_in() {
    // Get user data without logging in
    val userDataResponse =
        client
            .get()
            .uri(UserControllerUriFactory.getMePath())
            .exchange()
            .expectStatus()
            .isUnauthorized();
  }
}
