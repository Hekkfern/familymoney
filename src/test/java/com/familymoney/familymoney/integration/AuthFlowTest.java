package com.familymoney.familymoney.integration;

import com.familymoney.familymoney.services.IEmailSenderService;
import com.familymoney.familymoney.types.EmailVerificationToken;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import lombok.RequiredArgsConstructor;
import com.familymoney.familymoney.dtos.auth.RegisterRequestDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@RequiredArgsConstructor
public class AuthFlowTest {

    private final TestRestTemplate client;

    @MockitoBean
    private IEmailSenderService emailSenderService;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:17.6-alpine");

    private final String BASE_AUTH_URI = "/api/auth";

    @Test
    void Auth_SuccessfulRegistrationProcess() {
        // Mock email sender
        val verificationTokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailSenderService).sendEmailVerificationEmail(any(), any(), verificationTokenCaptor.capture());

        // register the new user
        val registerResponse =
                client.postForEntity(
                        String.format("%s/register", BASE_AUTH_URI),
                        new RegisterRequestDto(
                                "hectorfern1",
                                "hectorfern@gmail.com",
                                "YKE4DY2gn7jVQQ27XgND!"
                        ),
                        Void.class
                );
        assertNotNull(registerResponse);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNull(registerResponse.getBody());

        // get the captured email verification token
        val verificationToken = verificationTokenCaptor.getValue();

        // verify email
        val verifyEmailResponse =
                client.getForEntity(
                        String.format("%s/verify-email/%s", BASE_AUTH_URI, verificationToken),
                        Void.class, Void.class
                );
        assertNotNull(verifyEmailResponse);
        assertEquals(HttpStatus.OK, verifyEmailResponse.getStatusCode());
        assertNull(verifyEmailResponse.getBody());
    }
}
