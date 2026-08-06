package com.familymoney.domains.auth.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.domains.auth.events.EmailVerificationRequestedEvent;
import com.familymoney.domains.auth.exceptions.BlacklistedFamilyException;
import com.familymoney.domains.auth.exceptions.NewEmailVerificationTooSoonException;
import com.familymoney.domains.auth.exceptions.RefreshTokenInvalidException;
import com.familymoney.domains.auth.exceptions.RefreshTokenNotFoundException;
import com.familymoney.domains.auth.exceptions.RefreshTokenReuseDetectedException;
import com.familymoney.domains.auth.exceptions.UserAlreadyExistsException;
import com.familymoney.domains.auth.exceptions.UserNotEnabledException;
import com.familymoney.domains.auth.exceptions.VerificationTokenExpiredException;
import com.familymoney.domains.auth.exceptions.VerificationTokenNotFoundException;
import com.familymoney.domains.auth.repositories.IEmailVerificationRepository;
import com.familymoney.domains.auth.repositories.IPasswordResetRepository;
import com.familymoney.domains.auth.repositories.IRefreshTokenRepository;
import com.familymoney.domains.auth.repositories.ITokenFamilyBlacklistRepository;
import com.familymoney.domains.auth.repositories.IUsedRefreshTokenRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.CreateTokenFamilyBlacklistDto;
import com.familymoney.domains.auth.repositories.dtos.CreateUsedRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.repositories.entitites.TokenFamilyBlacklistEntity;
import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.repositories.IRoleRepository;
import com.familymoney.domains.users.repositories.IUserRepository;
import com.familymoney.domains.users.repositories.dtos.CreateUserDto;
import com.familymoney.domains.users.repositories.dtos.UpdateUserDto;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.properties.EmailVerificationProperties;
import com.familymoney.properties.JwtProperties;
import com.familymoney.security.JwtUtils;
import com.familymoney.security.UserPasswordEncoder;
import com.familymoney.testutils.FakeGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private IUserRepository userRepository;
  @Mock private IRoleRepository permissionsRepository;
  @Mock private IEmailSenderService emailSenderService;
  @Spy private UserPasswordEncoder passwordEncoder;
  @Spy private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

  @Spy
  private EmailVerificationProperties emailVerificationProperties =
      new EmailVerificationProperties(Duration.ofHours(1), Duration.ofMinutes(5));

  @Spy
  private JwtProperties jwtProperties =
      new JwtProperties("kdsfgsdajkhgfjhak", Duration.ofMinutes(5), Duration.ofHours(24));

  @Mock private IEmailVerificationRepository emailVerificationRepository;
  @Mock private IPasswordResetRepository passwordResetRepository;
  @Mock private ITokenFamilyBlacklistRepository tokenFamilyBlacklistRepository;
  @Mock private IUsedRefreshTokenRepository usedRefreshTokenRepository;
  @Mock private JwtUtils jwtUtils;
  @Mock private IRefreshTokenRepository refreshTokenRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private AuthService authService;

  private void mockUserRepositoryCreate() {
    when(userRepository.create(any(CreateUserDto.class)))
        .thenAnswer(
            invocation -> {
              CreateUserDto dto = invocation.getArgument(0, CreateUserDto.class);
              return Optional.of(
                  new UserEntity(
                      dto.id(), // return the same UserId received
                      dto.username(),
                      dto.email(),
                      dto.passwordHash(), // use dto.passwordHash() or provide a test value
                      now,
                      now,
                      false,
                      true));
            });
  }

  private void mockEmailVerificationRepositoryCreate() {
    when(emailVerificationRepository.create(any(CreateEmailVerificationDto.class)))
        .thenAnswer(
            invocation -> {
              CreateEmailVerificationDto dto =
                  invocation.getArgument(0, CreateEmailVerificationDto.class);
              return Optional.of(
                  new EmailVerificationEntity(dto.userId(), now, now, dto.expiresAt(), now));
            });
  }

  private void mockEmailVerificationRepositoryFindByUserId() {
    when(emailVerificationRepository.findByUserId(any(UserId.class)))
        .thenAnswer(
            invocation -> {
              UserId userid = invocation.getArgument(0, UserId.class);
              return Optional.of(
                  new EmailVerificationEntity(
                      userid, now, now, ExpirationTime.of(now.plusSeconds(3600)), now));
            });
  }

  private void mockRefreshTokenRepositoryCreate() {
    when(refreshTokenRepository.create(any(CreateRefreshTokenDto.class)))
        .thenAnswer(
            invocation -> {
              CreateRefreshTokenDto dto = invocation.getArgument(0, CreateRefreshTokenDto.class);
              return Optional.of(
                  new RefreshTokenEntity(
                      dto.id(), dto.userId(), now, now, dto.expiresAt(), dto.family()));
            });
  }

  private void mockUserRepositoryFindByEmail(
      final boolean isEnabled, final boolean isEmailVerified) {
    when(userRepository.findByEmail(any(Email.class)))
        .thenAnswer(
            invocation -> {
              Email email = invocation.getArgument(0, Email.class);
              return Optional.of(
                  new UserEntity(
                      UserId.generate(),
                      UserName.fromString(FakeGenerator.username()),
                      email,
                      "hashed-password",
                      now,
                      now,
                      isEmailVerified,
                      isEnabled));
            });
  }

  private void mockRefreshTokenFindByToken() {
    when(refreshTokenRepository.findByToken(any(RefreshToken.class)))
        .thenAnswer(
            invocation -> {
              RefreshToken refreshToken = invocation.getArgument(0, RefreshToken.class);
              return Optional.of(
                  new RefreshTokenEntity(
                      UUID.randomUUID(),
                      UserId.generate(),
                      now,
                      now,
                      ExpirationTime.of(now.plusSeconds(3600)),
                      TokenFamily.generate()));
            });
  }

  private void mockRefreshTokenOwner(final boolean isEnabled) {
    when(userRepository.findById(any(UserId.class)))
        .thenAnswer(
            invocation -> {
              UserId userId = invocation.getArgument(0, UserId.class);
              return Optional.of(
                  new UserEntity(
                      userId,
                      UserName.fromString(FakeGenerator.username()),
                      Email.fromString(FakeGenerator.email()),
                      "hashed-password",
                      now,
                      now,
                      true,
                      isEnabled));
            });
  }

  private void mockEmailVerificationTokenFindByToken() {
    when(emailVerificationRepository.findByToken(any(EmailVerificationToken.class)))
        .thenAnswer(
            invocation -> {
              EmailVerificationToken emailVerificationToken =
                  invocation.getArgument(0, EmailVerificationToken.class);
              return Optional.of(
                  new EmailVerificationEntity(
                      UserId.generate(), now, now, ExpirationTime.of(now.plusSeconds(3600)), now));
            });
  }

  @Nested
  class RegisterUser {

    @Test
    void succeeds_when_register_user_for_the_first_time() {
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(false);
      mockUserRepositoryCreate();
      mockEmailVerificationRepositoryCreate();

      assertThatCode(() -> authService.registerUser(username, email, password))
          .doesNotThrowAnyException();

      final ArgumentCaptor<CreateEmailVerificationDto> verificationDtoCaptor =
          ArgumentCaptor.forClass(CreateEmailVerificationDto.class);
      final ArgumentCaptor<EmailVerificationRequestedEvent> eventCaptor =
          ArgumentCaptor.forClass(EmailVerificationRequestedEvent.class);
      verify(userRepository).create(any(CreateUserDto.class));
      verify(emailVerificationRepository).create(verificationDtoCaptor.capture());
      verify(permissionsRepository).setRoleForUserId(any(UserId.class), any(Role.class));
      verify(eventPublisher).publishEvent(eventCaptor.capture());
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));

      final CreateEmailVerificationDto verificationDto = verificationDtoCaptor.getValue();
      final EmailVerificationRequestedEvent event = eventCaptor.getValue();
      assertThat(event.userId()).isEqualTo(verificationDto.userId());
      assertThat(event.email()).isEqualTo(email);
      assertThat(event.username()).isEqualTo(username);
      assertThat(event.verificationToken()).isEqualTo(verificationDto.token());
    }

    @Test
    void throws_when_user_already_exists() {
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(true);

      assertThrows(
          UserAlreadyExistsException.class,
          () -> authService.registerUser(username, email, password));
      verify(userRepository, never()).create(any(CreateUserDto.class));
      verify(eventPublisher, never()).publishEvent(any(EmailVerificationRequestedEvent.class));
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }

    @Test
    void throws_when_user_table_fails() {
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(false);
      when(userRepository.create(any(CreateUserDto.class))).thenReturn(Optional.empty());

      assertThrows(
          DatabaseExecutionException.class,
          () -> authService.registerUser(username, email, password));
      verify(eventPublisher, never()).publishEvent(any(EmailVerificationRequestedEvent.class));
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }

    @Test
    void fails_when_email_verification_table() {
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(false);
      mockUserRepositoryCreate();
      when(emailVerificationRepository.create(any(CreateEmailVerificationDto.class)))
          .thenReturn(Optional.empty());

      assertThrows(
          DatabaseExecutionException.class,
          () -> authService.registerUser(username, email, password));
      verify(eventPublisher, never()).publishEvent(any(EmailVerificationRequestedEvent.class));
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }
  }

  @Nested
  class LoginUser {

    @Test
    void returns_tokens_when_called_with_correct_credentials() {
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      mockUserRepositoryFindByEmail(true, true);
      when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
      when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
          .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));
      mockRefreshTokenRepositoryCreate();

      assertThatCode(
              () -> {
                final TokenPair tokens = authService.loginUser(email, password);
                assertNotNull(tokens);
                assertNotNull(tokens.accessToken());
                assertNotNull(tokens.refreshToken());
              })
          .doesNotThrowAnyException();

      verify(refreshTokenRepository).create(any(CreateRefreshTokenDto.class));
    }

    @Test
    void throws_when_called_with_incorrect_password() {
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      mockUserRepositoryFindByEmail(true, true);
      when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(false);

      assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));

      verify(passwordEncoder).verify(eq(password.value()), anyString());
      verify(passwordEncoder, never()).verifyDummyPassword(anyString());
      verify(jwtUtils, never()).generateAccessToken(any(UserId.class), any(TokenFamily.class));
      verify(refreshTokenRepository, never()).create(any(CreateRefreshTokenDto.class));
    }

    @Test
    void throws_when_no_user_with_that_email_exists() {
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
      doReturn(false).when(passwordEncoder).verifyDummyPassword(password.value());

      assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));

      verify(passwordEncoder).verifyDummyPassword(password.value());
      verify(passwordEncoder, never()).verify(anyString(), anyString());
      verify(jwtUtils, never()).generateAccessToken(any(UserId.class), any(TokenFamily.class));
      verify(refreshTokenRepository, never()).create(any(CreateRefreshTokenDto.class));
    }

    @Test
    void throws_when_refresh_token_table_fails() {
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      mockUserRepositoryFindByEmail(true, true);
      when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
      when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
          .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));
      when(refreshTokenRepository.create(any(CreateRefreshTokenDto.class)))
          .thenReturn(Optional.empty());

      assertThrows(DatabaseExecutionException.class, () -> authService.loginUser(email, password));
    }

    @Test
    void throws_when_user_has_not_verified_email() {
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      mockUserRepositoryFindByEmail(true, false);
      when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(false);

      assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));

      verify(passwordEncoder).verify(eq(password.value()), anyString());
      verify(passwordEncoder, never()).verifyDummyPassword(anyString());
      verify(jwtUtils, never()).generateAccessToken(any(UserId.class), any(TokenFamily.class));
      verify(refreshTokenRepository, never()).create(any(CreateRefreshTokenDto.class));
    }

    @Test
    void throws_when_user_is_not_enabled() {
      final Email email = Email.fromString(FakeGenerator.email());
      final Password password = Password.fromString(FakeGenerator.password());

      mockUserRepositoryFindByEmail(false, true);
      when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(false);

      assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));

      verify(passwordEncoder).verify(eq(password.value()), anyString());
      verify(passwordEncoder, never()).verifyDummyPassword(anyString());
      verify(jwtUtils, never()).generateAccessToken(any(UserId.class), any(TokenFamily.class));
      verify(refreshTokenRepository, never()).create(any(CreateRefreshTokenDto.class));
    }
  }

  @Nested
  class RefreshTokens {

    @Test
    void succeeds_when_refresh_token_is_valid() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      mockRefreshTokenOwner(true);
      when(usedRefreshTokenRepository.create(any(CreateUsedRefreshTokenDto.class)))
          .thenReturn(Optional.of(new UsedRefreshTokenEntity(TokenFamily.generate(), now, now)));
      when(refreshTokenRepository.updateByToken(
              any(RefreshToken.class), any(UpdateRefreshTokenDto.class)))
          .thenReturn(true);
      when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
          .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));

      assertThatCode(
              () -> {
                final TokenPair tokens = authService.refreshTokens(refreshToken);
                assertNotNull(tokens);
                assertNotNull(tokens.accessToken());
                assertNotNull(tokens.refreshToken());
              })
          .doesNotThrowAnyException();

      final InOrder inOrder = inOrder(usedRefreshTokenRepository, refreshTokenRepository);
      inOrder
          .verify(usedRefreshTokenRepository)
          .create(argThat(data -> data.token().equals(refreshToken)));
      inOrder
          .verify(refreshTokenRepository)
          .updateByToken(any(RefreshToken.class), any(UpdateRefreshTokenDto.class));
    }

    @Test
    void throws_when_refresh_token_doesnt_exist() {
      final RefreshToken refreshToken = RefreshToken.generate();

      when(refreshTokenRepository.findByToken(any(RefreshToken.class)))
          .thenReturn(Optional.empty());
      when(usedRefreshTokenRepository.findByToken(any(RefreshToken.class)))
          .thenReturn(Optional.empty());

      assertThrows(
          RefreshTokenNotFoundException.class, () -> authService.refreshTokens(refreshToken));
    }

    @Test
    void throws_when_updating_refresh_token_fails() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      mockRefreshTokenOwner(true);
      when(usedRefreshTokenRepository.create(any(CreateUsedRefreshTokenDto.class)))
          .thenReturn(Optional.of(new UsedRefreshTokenEntity(TokenFamily.generate(), now, now)));
      when(refreshTokenRepository.updateByToken(any(), any())).thenReturn(false);
      when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
          .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));

      assertThrows(DatabaseExecutionException.class, () -> authService.refreshTokens(refreshToken));
    }

    @Test
    void detects_reuse_when_refresh_token_was_previously_used() {
      final RefreshToken refreshToken = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());
      when(usedRefreshTokenRepository.findByToken(refreshToken))
          .thenReturn(Optional.of(new UsedRefreshTokenEntity(family, now, now)));
      when(tokenFamilyBlacklistRepository.create(any(CreateTokenFamilyBlacklistDto.class)))
          .thenReturn(Optional.of(new TokenFamilyBlacklistEntity(family, now)));

      assertThrows(
          RefreshTokenReuseDetectedException.class, () -> authService.refreshTokens(refreshToken));

      verify(tokenFamilyBlacklistRepository).create(new CreateTokenFamilyBlacklistDto(family));
      verify(refreshTokenRepository, never())
          .updateByToken(any(RefreshToken.class), any(UpdateRefreshTokenDto.class));
      verify(jwtUtils, never()).generateAccessToken(any(UserId.class), any(TokenFamily.class));
    }

    @Test
    void detects_reuse_when_refresh_token_reservation_fails() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      mockRefreshTokenOwner(true);
      when(usedRefreshTokenRepository.create(any(CreateUsedRefreshTokenDto.class)))
          .thenReturn(Optional.empty());
      when(tokenFamilyBlacklistRepository.create(any(CreateTokenFamilyBlacklistDto.class)))
          .thenAnswer(
              invocation -> {
                final CreateTokenFamilyBlacklistDto dto =
                    invocation.getArgument(0, CreateTokenFamilyBlacklistDto.class);
                return Optional.of(new TokenFamilyBlacklistEntity(dto.family(), now));
              });

      assertThrows(
          RefreshTokenReuseDetectedException.class, () -> authService.refreshTokens(refreshToken));

      verify(refreshTokenRepository, never())
          .updateByToken(any(RefreshToken.class), any(UpdateRefreshTokenDto.class));
      verify(jwtUtils, never()).generateAccessToken(any(UserId.class), any(TokenFamily.class));
    }

    @Test
    void detects_reuse_when_family_is_already_blacklisted() {
      final RefreshToken refreshToken = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());
      when(usedRefreshTokenRepository.findByToken(refreshToken))
          .thenReturn(Optional.of(new UsedRefreshTokenEntity(family, now, now)));
      when(tokenFamilyBlacklistRepository.create(any(CreateTokenFamilyBlacklistDto.class)))
          .thenReturn(Optional.empty());

      assertThrows(
          RefreshTokenReuseDetectedException.class, () -> authService.refreshTokens(refreshToken));
    }

    @Test
    void throws_when_refresh_token_is_expired() {
      final RefreshToken refreshToken = RefreshToken.generate();

      when(refreshTokenRepository.findByToken(any(RefreshToken.class)))
          .thenAnswer(
              invocation -> {
                RefreshToken rf = invocation.getArgument(0, RefreshToken.class);
                return Optional.of(
                    new RefreshTokenEntity(
                        UUID.randomUUID(),
                        UserId.generate(),
                        now.minusSeconds(3600),
                        now,
                        ExpirationTime.of(now.minusSeconds(10)),
                        TokenFamily.generate()));
              });

      assertThrows(
          RefreshTokenInvalidException.class, () -> authService.refreshTokens(refreshToken));
    }

    @Test
    void throws_when_user_is_disabled() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      mockRefreshTokenOwner(false);

      assertThrows(UserNotEnabledException.class, () -> authService.refreshTokens(refreshToken));

      verify(refreshTokenRepository, never())
          .updateByToken(any(RefreshToken.class), any(UpdateRefreshTokenDto.class));
      verify(jwtUtils, never()).generateAccessToken(any(UserId.class), any(TokenFamily.class));
    }

    @Test
    void throws_when_family_is_blacklisted() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      mockRefreshTokenOwner(true);
      when(tokenFamilyBlacklistRepository.exists(any(TokenFamily.class))).thenReturn(true);

      assertThrows(BlacklistedFamilyException.class, () -> authService.refreshTokens(refreshToken));

      verify(refreshTokenRepository, never())
          .updateByToken(any(RefreshToken.class), any(UpdateRefreshTokenDto.class));
    }
  }

  @Nested
  class VerifyEmail {

    @Test
    void succeeds_when_called_with_valid_email_verification_token() {
      final EmailVerificationToken emailVerificationToken =
          EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

      mockEmailVerificationTokenFindByToken();
      when(userRepository.updateById(any(UserId.class), any(UpdateUserDto.class))).thenReturn(true);
      when(emailVerificationRepository.deleteByUserId(any(UserId.class))).thenReturn(true);

      assertThatCode(() -> authService.verifyEmail(emailVerificationToken))
          .doesNotThrowAnyException();

      verify(userRepository).updateById(any(UserId.class), any(UpdateUserDto.class));
      verify(emailVerificationRepository).deleteByUserId(any(UserId.class));
    }

    @Test
    void throws_when_email_verification_token_doesnt_exist() {
      final EmailVerificationToken emailVerificationToken =
          EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

      when(emailVerificationRepository.findByToken(emailVerificationToken))
          .thenReturn(Optional.empty());

      assertThrows(
          VerificationTokenNotFoundException.class,
          () -> authService.verifyEmail(emailVerificationToken));
    }

    @Test
    void throws_when_email_verification_token_is_expired() {
      final EmailVerificationToken emailVerificationToken =
          EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

      when(emailVerificationRepository.findByToken(any(EmailVerificationToken.class)))
          .thenAnswer(
              invocation -> {
                EmailVerificationToken evt =
                    invocation.getArgument(0, EmailVerificationToken.class);
                return Optional.of(
                    new EmailVerificationEntity(
                        UserId.generate(),
                        now.minusSeconds(3600),
                        now.minusSeconds(3600),
                        ExpirationTime.of(now.minusSeconds(1)),
                        now.minusSeconds(3600)));
              });

      assertThrows(
          VerificationTokenExpiredException.class,
          () -> authService.verifyEmail(emailVerificationToken));

      verify(userRepository, never()).updateById(any(UserId.class), any(UpdateUserDto.class));
      verify(emailVerificationRepository, never()).deleteByUserId(any(UserId.class));
    }

    @Test
    void throws_when_updating_user_fails() {
      final EmailVerificationToken emailVerificationToken =
          EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

      mockEmailVerificationTokenFindByToken();
      when(userRepository.updateById(any(UserId.class), any(UpdateUserDto.class)))
          .thenReturn(false);

      assertThrows(
          DatabaseExecutionException.class, () -> authService.verifyEmail(emailVerificationToken));

      verify(userRepository).updateById(any(UserId.class), any(UpdateUserDto.class));
      verify(emailVerificationRepository, never()).deleteByUserId(any(UserId.class));
    }

    @Test
    void throws_when_deleting_email_verification_token_fails() {
      final EmailVerificationToken emailVerificationToken =
          EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

      mockEmailVerificationTokenFindByToken();
      when(userRepository.updateById(any(UserId.class), any(UpdateUserDto.class))).thenReturn(true);
      when(emailVerificationRepository.deleteByUserId(any(UserId.class))).thenReturn(false);

      assertThrows(
          DatabaseExecutionException.class, () -> authService.verifyEmail(emailVerificationToken));

      verify(userRepository).updateById(any(UserId.class), any(UpdateUserDto.class));
      verify(emailVerificationRepository).deleteByUserId(any(UserId.class));
    }
  }

  @Nested
  class ResendVerificationEmail {

    @Test
    void sends_when_last_attempt_was_long_time_ago() {
      final Email email = Email.fromString(FakeGenerator.email());

      mockUserRepositoryFindByEmail(true, false);
      when(emailVerificationRepository.findByUserId(any(UserId.class)))
          .thenReturn(
              Optional.of(
                  new EmailVerificationEntity(
                      UserId.generate(),
                      now.minusSeconds(3600),
                      now.minusSeconds(3600),
                      ExpirationTime.of(now.plusSeconds(100)),
                      now.minusSeconds(3600))));
      when(emailVerificationRepository.updateByUserId(
              any(UserId.class), any(UpdateEmailVerificationTokenDto.class)))
          .thenReturn(true);

      assertThatCode(() -> authService.resendVerificationEmail(email)).doesNotThrowAnyException();

      verify(emailVerificationRepository)
          .updateByUserId(
              any(UserId.class),
              argThat(
                  dto ->
                      dto.lastSentAt().equals(now)
                          && dto.token() != null
                          && dto.expiresAt() != null));
      verify(emailSenderService)
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }

    @Test
    void throws_when_last_attempt_was_recently() {
      final Email email = Email.fromString(FakeGenerator.email());

      mockUserRepositoryFindByEmail(true, false);
      mockEmailVerificationRepositoryFindByUserId();

      assertThrows(
          NewEmailVerificationTooSoonException.class,
          () -> authService.resendVerificationEmail(email));

      verify(emailVerificationRepository, never())
          .updateByUserId(any(UserId.class), any(UpdateEmailVerificationTokenDto.class));
      verify(emailVerificationRepository, never()).create(any(CreateEmailVerificationDto.class));
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }

    @Test
    void throws_when_updating_email_verification_token_fails() {
      final Email email = Email.fromString(FakeGenerator.email());

      mockUserRepositoryFindByEmail(true, false);
      when(emailVerificationRepository.findByUserId(any(UserId.class)))
          .thenReturn(
              Optional.of(
                  new EmailVerificationEntity(
                      UserId.generate(),
                      now.minusSeconds(3600),
                      now.minusSeconds(3600),
                      ExpirationTime.of(now.plusSeconds(100)),
                      now.minusSeconds(3600))));
      when(emailVerificationRepository.updateByUserId(
              any(UserId.class), any(UpdateEmailVerificationTokenDto.class)))
          .thenReturn(false);

      assertThrows(
          DatabaseExecutionException.class, () -> authService.resendVerificationEmail(email));

      verify(emailVerificationRepository, never()).create(any(CreateEmailVerificationDto.class));
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }

    @Test
    void does_nothing_when_no_user_with_that_email_exists() {
      final Email email = Email.fromString(FakeGenerator.email());
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      assertThatCode(() -> authService.resendVerificationEmail(email)).doesNotThrowAnyException();

      verify(emailVerificationRepository, never()).findByUserId(any(UserId.class));
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }

    @Test
    void does_nothing_when_user_email_is_already_verified() {
      final Email email = Email.fromString(FakeGenerator.email());
      mockUserRepositoryFindByEmail(true, true);

      assertThatCode(() -> authService.resendVerificationEmail(email)).doesNotThrowAnyException();

      verify(emailVerificationRepository, never()).findByUserId(any(UserId.class));
      verify(emailSenderService, never())
          .sendEmailVerificationEmail(
              any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
    }
  }

  @Nested
  class ForgotPassword {
    // TODO
  }

  @Nested
  class ResetPassword {
    // TODO
  }

  @Nested
  class LogoutUser {

    @Test
    void succeeds_when_token_is_valid() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      when(refreshTokenRepository.deleteByToken(any(RefreshToken.class))).thenReturn(true);
      when(tokenFamilyBlacklistRepository.create(any(CreateTokenFamilyBlacklistDto.class)))
          .thenReturn(Optional.of(new TokenFamilyBlacklistEntity(TokenFamily.generate(), now)));

      assertThatCode(() -> authService.logoutUser(refreshToken)).doesNotThrowAnyException();

      verify(refreshTokenRepository).deleteByToken(any(RefreshToken.class));
      verify(tokenFamilyBlacklistRepository).create(any(CreateTokenFamilyBlacklistDto.class));
    }

    @Test
    void throws_when_refresh_token_doesnt_exist() {
      final RefreshToken refreshToken = RefreshToken.generate();

      when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

      assertThrows(RefreshTokenNotFoundException.class, () -> authService.logoutUser(refreshToken));

      verify(refreshTokenRepository, never()).deleteByToken(any(RefreshToken.class));
      verify(tokenFamilyBlacklistRepository, never())
          .create(any(CreateTokenFamilyBlacklistDto.class));
    }

    @Test
    void throws_when_refresh_token_is_expired() {
      final RefreshToken refreshToken = RefreshToken.generate();

      when(refreshTokenRepository.findByToken(any(RefreshToken.class)))
          .thenAnswer(
              invocation -> {
                RefreshToken rt = invocation.getArgument(0, RefreshToken.class);
                return Optional.of(
                    new RefreshTokenEntity(
                        UUID.randomUUID(),
                        UserId.generate(),
                        now.minusSeconds(7200),
                        now.minusSeconds(7200),
                        ExpirationTime.of(now.minusSeconds(1)),
                        TokenFamily.generate()));
              });

      assertThrows(RefreshTokenInvalidException.class, () -> authService.logoutUser(refreshToken));

      verify(refreshTokenRepository, never()).deleteByToken(any(RefreshToken.class));
      verify(tokenFamilyBlacklistRepository, never())
          .create(any(CreateTokenFamilyBlacklistDto.class));
    }

    @Test
    void throws_when_family_is_blacklisted() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      when(tokenFamilyBlacklistRepository.exists(any(TokenFamily.class))).thenReturn(true);

      assertThrows(BlacklistedFamilyException.class, () -> authService.logoutUser(refreshToken));

      verify(refreshTokenRepository, never()).deleteByToken(any(RefreshToken.class));
      verify(tokenFamilyBlacklistRepository, never())
          .create(any(CreateTokenFamilyBlacklistDto.class));
    }

    @Test
    void throws_when_deleting_refresh_token_fails() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      when(refreshTokenRepository.deleteByToken(any(RefreshToken.class))).thenReturn(false);

      assertThrows(DatabaseExecutionException.class, () -> authService.logoutUser(refreshToken));

      verify(refreshTokenRepository).deleteByToken(any(RefreshToken.class));
      verify(tokenFamilyBlacklistRepository, never()).deleteByFamily(any(TokenFamily.class));
    }

    @Test
    void succeeds_when_family_blacklisting_conflicts() {
      final RefreshToken refreshToken = RefreshToken.generate();

      mockRefreshTokenFindByToken();
      when(refreshTokenRepository.deleteByToken(any(RefreshToken.class))).thenReturn(true);
      when(tokenFamilyBlacklistRepository.create(any(CreateTokenFamilyBlacklistDto.class)))
          .thenReturn(Optional.empty());

      assertThatCode(() -> authService.logoutUser(refreshToken)).doesNotThrowAnyException();

      verify(refreshTokenRepository).deleteByToken(any(RefreshToken.class));
      verify(tokenFamilyBlacklistRepository).create(any(CreateTokenFamilyBlacklistDto.class));
    }
  }

  @Nested
  class IsFamilyBlacklisted {

    @Test
    void returns_true_when_family_is_blacklisted() {
      final TokenFamily family = TokenFamily.generate();
      when(tokenFamilyBlacklistRepository.exists(family)).thenReturn(true);

      assertThat(authService.isFamilyBlacklisted(family)).isTrue();
    }

    @Test
    void returns_false_when_family_is_not_blacklisted() {
      final TokenFamily family = TokenFamily.generate();
      when(tokenFamilyBlacklistRepository.exists(family)).thenReturn(false);

      assertThat(authService.isFamilyBlacklisted(family)).isFalse();
    }
  }
}
