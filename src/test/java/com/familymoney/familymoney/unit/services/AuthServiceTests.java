package com.familymoney.familymoney.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.UserAlreadyExistsException;
import com.familymoney.familymoney.repositories.IEmailVerificationRepository;
import com.familymoney.familymoney.repositories.IRolesRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.services.AuthService;
import com.familymoney.familymoney.services.IEmailSenderService;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AuthServiceTests {

  @Mock private IUserRepository userRepository;
  @Mock private IRolesRepository permissionsRepository;
  @Mock private IEmailSenderService emailSenderService;
  @Spy private UserPasswordEncoder passwordEncoder;
  @Mock private IEmailVerificationRepository emailVerificationRepository;

  @InjectMocks private AuthService authService;

  // region registerUser

  @Test
  public void register_user_for_the_first_time_it_succeeds() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(false);
    when(userRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                new UserDbo(
                    new UserId(UUID.randomUUID()),
                    new Username("testuser"),
                    new Email("test@mail.com"),
                    "dsafjhadskjgf5dsf56a4",
                    Instant.now(),
                    Instant.now(),
                    false,
                    true)));
    when(emailVerificationRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                new EmailVerificationDbo(
                    UUID.randomUUID(),
                    new UserId(UUID.randomUUID()),
                    new EmailVerificationToken("verificationcode"),
                    Instant.now(),
                    Instant.now().plusSeconds(3600))));

    assertDoesNotThrow(
        () ->
            authService.registerUser(
                new Username(FakeGenerator.username()),
                new Email(FakeGenerator.email()),
                new Password(FakeGenerator.password())));

    verify(userRepository, times(1)).create(any(), any(), any());
    verify(emailVerificationRepository, times(1)).create(any(), any(), any());
    verify(permissionsRepository, times(1)).setRoleForUserId(any(), any());
  }

  @Test
  public void register_user_when_it_already_exists() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(true);

    assertThrows(
        UserAlreadyExistsException.class,
        () ->
            authService.registerUser(
                new Username(FakeGenerator.username()),
                new Email(FakeGenerator.email()),
                new Password(FakeGenerator.password())));
  }

  @Test
  public void register_user_but_user_table_fails() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(false);
    when(userRepository.create(any(), any(), any())).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () ->
            authService.registerUser(
                new Username(FakeGenerator.username()),
                new Email(FakeGenerator.email()),
                new Password(FakeGenerator.password())));
  }

  @Test
  public void register_user_but_email_verification_table_fails() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(false);
    when(userRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                new UserDbo(
                    new UserId(UUID.randomUUID()),
                    new Username("testuser"),
                    new Email("test@mail.com"),
                    "dsafjhadskjgf5dsf56a4",
                    Instant.now(),
                    Instant.now(),
                    false,
                    true)));
    when(emailVerificationRepository.create(any(), any(), any())).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () ->
            authService.registerUser(
                new Username(FakeGenerator.username()),
                new Email(FakeGenerator.email()),
                new Password(FakeGenerator.password())));
  }

  // endregion
}
