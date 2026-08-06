package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.exceptions.BlacklistedFamilyException;
import com.familymoney.domains.auth.exceptions.EmailAlreadyVerifiedException;
import com.familymoney.domains.auth.exceptions.EmailNotFoundException;
import com.familymoney.domains.auth.exceptions.NewEmailVerificationTooSoonException;
import com.familymoney.domains.auth.exceptions.RefreshTokenInvalidException;
import com.familymoney.domains.auth.exceptions.RefreshTokenNotFoundException;
import com.familymoney.domains.auth.exceptions.UserAlreadyExistsException;
import com.familymoney.domains.auth.exceptions.UserNotEnabledException;
import com.familymoney.domains.auth.exceptions.VerificationTokenExpiredException;
import com.familymoney.domains.auth.exceptions.VerificationTokenNotFoundException;
import com.familymoney.domains.auth.repositories.IEmailVerificationRepository;
import com.familymoney.domains.auth.repositories.IPasswordResetRepository;
import com.familymoney.domains.auth.repositories.IRefreshTokenRepository;
import com.familymoney.domains.auth.repositories.ITokenFamilyBlacklistRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.CreateTokenFamilyBlacklistDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.repositories.entitites.TokenFamilyBlacklistEntity;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
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
import com.familymoney.testutils.UUIDGenerator;
import java.time.Clock;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

  private final IUserRepository userRepository;
  private final UserPasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final IEmailSenderService emailSenderService;
  private final IRefreshTokenRepository refreshTokenRepository;
  private final IEmailVerificationRepository emailVerificationRepository;
  private final IPasswordResetRepository passwordResetRepository;
  private final IRoleRepository roleRepository;
  private final Clock clock;
  private final JwtProperties jwtProperties;
  private final EmailVerificationProperties emailVerificationProperties;
  private final ITokenFamilyBlacklistRepository tokenFamilyBlacklistRepository;

  @Transactional
  @Override
  public void registerUser(final UserName username, final Email email, final Password password) {
    log.trace("registerUser() started");
    // Check if user already exists
    if (userRepository.existsByEmailOrUsername(email, username)) {
      throw new UserAlreadyExistsException("A user with that email or username already exists.");
    }
    // Create user
    final UserId userId = UserId.generate();
    userRepository
        .create(
            new CreateUserDto(
                userId, username, email, passwordEncoder.encode(password.value()), true, false))
        .orElseThrow(() -> new DatabaseExecutionException("Could not create user in the database"));
    // Assign user permissions (default role)
    roleRepository.setRoleForUserId(userId, Role.USER);
    // Generate and save verification token to database
    final EmailVerificationEntity emailVerificationTokenDb =
        emailVerificationRepository
            .create(
                new CreateEmailVerificationDto(
                    userId,
                    EmailVerificationToken.generate(),
                    ExpirationTime.of(
                        Instant.now(clock).plus(emailVerificationProperties.tokenDuration()))))
            .orElseThrow(
                () ->
                    new DatabaseExecutionException(
                        "Could not create email verification token in the database"));
    // Send verification email asynchronously
    emailSenderService.sendEmailVerificationEmail(
        email, username, emailVerificationTokenDb.token());
    log.trace("registerUser() completed");
  }

  @Override
  public TokenPair loginUser(final Email email, final Password password) {
    log.trace("loginUser() started");
    // Find user by email
    final UserEntity userDb =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Email doesn't exist"));
    // Verify that user is enabled
    if (!userDb.isEnabled()) {
      throw new BadCredentialsException("User is not enabled");
    }
    // Verify that email is verified
    if (!userDb.isEmailVerified()) {
      throw new BadCredentialsException("Email is not verified");
    }
    // Verify password
    if (!passwordEncoder.verify(password.value(), userDb.hashedPassword())) {
      throw new BadCredentialsException("Wrong password for the given email");
    }
    // Generate family ID for the tokens of this session
    final TokenFamily family = TokenFamily.generate();
    // Generate access token
    final AccessToken accessToken = jwtUtils.generateAccessToken(userDb.id(), family);
    // Generate refresh token
    final RefreshToken refreshToken = RefreshToken.generate();
    // Save refresh token in database
    refreshTokenRepository
        .create(
            new CreateRefreshTokenDto(
                UUIDGenerator.generate(),
                userDb.id(),
                refreshToken,
                family,
                ExpirationTime.of(Instant.now(clock).plus(jwtProperties.refreshTokenDuration()))))
        .orElseThrow(
            () -> new DatabaseExecutionException("Could not create refresh token in the database"));
    // Build response with both tokens
    log.trace("loginUser() finished");
    return new TokenPair(accessToken, refreshToken);
  }

  @Override
  public TokenPair refreshTokens(final RefreshToken refreshToken) {
    log.trace("refreshTokens() started");
    // Find the refresh token in the database
    final RefreshTokenEntity refreshTokenDb =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(() -> new NoSuchElementException("Refresh token not found"));
    // Check if the refresh token is expired
    if (refreshTokenDb.expiresAt().isExpired(clock)) {
      final String msg = "Expired refresh token";
      log.info(msg);
      throw new RefreshTokenInvalidException(msg);
    }
    // check if the user is enabled
    final UserEntity userDb =
        userRepository
            .findById(refreshTokenDb.userId())
            .orElseThrow(() -> new BadCredentialsException("User does not exist"));
    if (!userDb.isEnabled()) {
      throw new UserNotEnabledException();
    }
    // Check if the family token is blacklisted
    if (tokenFamilyBlacklistRepository.exists(refreshTokenDb.family())) {
      final String msg = "Token family is blacklisted";
      log.info(msg);
      throw new BlacklistedFamilyException(msg);
    }
    // Generate new tokens
    final AccessToken newAccessToken =
        jwtUtils.generateAccessToken(refreshTokenDb.userId(), refreshTokenDb.family());
    final RefreshToken newRefreshToken = RefreshToken.generate();
    // Save new refresh token in database
    final boolean refreshTokenUpdated =
        refreshTokenRepository.updateByToken(
            refreshToken,
            UpdateRefreshTokenDto.builder()
                .token(newRefreshToken)
                .expiresAt(
                    ExpirationTime.of(
                        Instant.now(clock).plus(jwtProperties.refreshTokenDuration())))
                .build());
    if (!refreshTokenUpdated) {
      throw new DatabaseExecutionException("Could not create new refresh token in the database");
    }
    // Build response with both tokens
    log.trace("refreshTokens() finished");
    return new TokenPair(newAccessToken, newRefreshToken);
  }

  @Override
  public void verifyEmail(final EmailVerificationToken token) {
    // Find the verification token in the database
    final EmailVerificationEntity emailVerificationTokenDb =
        emailVerificationRepository
            .findByToken(token)
            .orElseThrow(
                () -> new VerificationTokenNotFoundException("Invalid email verification token"));
    // Check if the token is expired
    if (emailVerificationTokenDb.expiresAt().isExpired(clock)) {
      throw new VerificationTokenExpiredException("Email verification token has expired");
    }
    // Verify the user's email
    final boolean userUpdated =
        userRepository.updateById(
            emailVerificationTokenDb.userId(),
            UpdateUserDto.builder().isEmailVerified(true).build());
    if (!userUpdated) {
      throw new DatabaseExecutionException(
          "Could not set the is_email_verified column of the user in the database");
    }
    // Delete all the verification tokens assigned to this user from the database
    final boolean tokenDeleted =
        emailVerificationRepository.deleteByUserId(emailVerificationTokenDb.userId());
    if (!tokenDeleted) {
      throw new DatabaseExecutionException(
          "Could not delete the email verification token in the database");
    }
  }

  @Override
  public void resendVerificationEmail(final Email email) {
    // Look for user by email
    final UserEntity userDb =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmailNotFoundException("User with that email not found"));
    // Check if user is already verified. If so, skip
    if (userDb.isEmailVerified()) {
      throw new EmailAlreadyVerifiedException("Email is already verified");
    }
    // check minimum wait time between requests. If the current instant is before the createdAt
    // plus the configured wait time, the user is requesting a new verification email too soon.
    final Optional<EmailVerificationEntity> oldEmailVerificationTokenDb =
        emailVerificationRepository.findByUserId(userDb.id());
    if (oldEmailVerificationTokenDb.isPresent()
        && Instant.now(clock)
            .isBefore(
                oldEmailVerificationTokenDb
                    .get()
                    .lastSentAt()
                    .plus(emailVerificationProperties.waitTime()))) {
      throw new NewEmailVerificationTooSoonException();
    }
    // Generate and save verification token to database
    final EmailVerificationToken newEmailVerificationToken = EmailVerificationToken.generate();
    final boolean emailVerificationTokenUpdated =
        emailVerificationRepository.updateByUserId(
            userDb.id(),
            UpdateEmailVerificationTokenDto.builder()
                .token(newEmailVerificationToken)
                .expiresAt(
                    ExpirationTime.of(
                        Instant.now(clock).plus(emailVerificationProperties.tokenDuration())))
                .lastSentAt(Instant.now(clock))
                .build());
    if (!emailVerificationTokenUpdated) {
      throw new DatabaseExecutionException(
          "Could not create email verification token in the database");
    }
    // Send verification email
    emailSenderService.sendEmailVerificationEmail(
        email, userDb.username(), newEmailVerificationToken);
  }

  @Override
  public void forgotPassword(final Email email) {
    // TODO
  }

  @Override
  public void resetPassword(final PasswordResetToken token, final Password newPassword) {
    // TODO
  }

  @Transactional
  @Override
  public void logoutUser(final RefreshToken refreshToken) {
    // Find the refresh token in the database
    final RefreshTokenEntity refreshTokenDb =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(
                () -> new RefreshTokenNotFoundException("Refresh token not found in the database"));
    // Check if the refresh token is expired
    if (refreshTokenDb.expiresAt().isExpired(clock)) {
      final String msg = "Expired refresh token";
      log.trace(msg);
      throw new RefreshTokenInvalidException(msg);
    }
    // Check if the family token is blacklisted
    if (tokenFamilyBlacklistRepository.exists(refreshTokenDb.family())) {
      final String msg = "Token family is blacklisted";
      log.info(msg);
      throw new BlacklistedFamilyException(msg);
    }
    // Invalidate refresh token from the database
    final boolean refreshTokenDeleted = refreshTokenRepository.deleteByToken(refreshToken);
    if (!refreshTokenDeleted) {
      throw new DatabaseExecutionException("Could not delete the refresh token in the database");
    }
    // Invalidate any existing access token
    final Optional<TokenFamilyBlacklistEntity> accessTokenBlacklisted =
        tokenFamilyBlacklistRepository.create(
            new CreateTokenFamilyBlacklistDto(refreshTokenDb.family()));
    if (accessTokenBlacklisted.isEmpty()) {
      throw new DatabaseExecutionException(
          "Could not blacklist the family of tokens in the database");
    }
  }

  @Override
  public boolean isFamilyBlacklisted(final TokenFamily family) {
    return tokenFamilyBlacklistRepository.exists(family);
  }
}
