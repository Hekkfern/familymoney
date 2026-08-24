package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.events.EmailVerificationRequestedEvent;
import com.familymoney.domains.auth.events.PasswordResetRequestedEvent;
import com.familymoney.domains.auth.exceptions.BlacklistedFamilyException;
import com.familymoney.domains.auth.exceptions.NewEmailVerificationTooSoonException;
import com.familymoney.domains.auth.exceptions.RefreshTokenInvalidException;
import com.familymoney.domains.auth.exceptions.RefreshTokenNotFoundException;
import com.familymoney.domains.auth.exceptions.RefreshTokenReuseDetectedException;
import com.familymoney.domains.auth.exceptions.ResetPasswordTokenExpiredException;
import com.familymoney.domains.auth.exceptions.ResetPasswordTokenNotFoundException;
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
import com.familymoney.domains.auth.repositories.dtos.CreatePasswordResetDto;
import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.CreateTokenFamilyBlacklistDto;
import com.familymoney.domains.auth.repositories.dtos.CreateUsedRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdatePasswordResetDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
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
import com.familymoney.properties.ResetPasswordProperties;
import com.familymoney.security.JwtUtils;
import com.familymoney.security.UserPasswordEncoder;
import com.familymoney.utils.UUIDGenerator;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

  private static final String USER_ALREADY_EXISTS_MESSAGE =
      "A user with that email or username already exists.";
  private static final String USERNAME_UNIQUE_CONSTRAINT = "users_username_key";
  private static final String EMAIL_UNIQUE_CONSTRAINT = "users_email_key";

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
  private final ResetPasswordProperties resetPasswordProperties;
  private final ITokenFamilyBlacklistRepository tokenFamilyBlacklistRepository;
  private final IUsedRefreshTokenRepository usedRefreshTokenRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Override
  public void registerUser(final UserName username, final Email email, final Password password) {
    log.trace("registerUser() started");
    // Create user
    final UserId userId = UserId.generate();
    try {
      userRepository
          .create(
              new CreateUserDto(
                  userId, username, email, passwordEncoder.encode(password.value()), true, false))
          .orElseThrow(
              () -> new DatabaseExecutionException("Could not create user in the database"));
    } catch (final DuplicateKeyException exception) {
      if (!isUserIdentityConflict(exception)) {
        throw new DatabaseExecutionException("Could not create user in the database", exception);
      }
      throw new UserAlreadyExistsException(USER_ALREADY_EXISTS_MESSAGE, exception);
    }
    // Assign user permissions (default role)
    final boolean defaultRoleAssigned = roleRepository.setRoleForUserId(userId, Role.USER);
    if (!defaultRoleAssigned) {
      throw new DatabaseExecutionException("Could not assign the default role to the user");
    }
    // Generate and save verification token to database
    final EmailVerificationToken emailVerificationToken = EmailVerificationToken.generate();
    emailVerificationRepository
        .create(
            new CreateEmailVerificationDto(
                userId,
                emailVerificationToken,
                ExpirationTime.of(
                    Instant.now(clock).plus(emailVerificationProperties.tokenDuration()))))
        .orElseThrow(
            () ->
                new DatabaseExecutionException(
                    "Could not create email verification token in the database"));
    eventPublisher.publishEvent(
        new EmailVerificationRequestedEvent(userId, email, username, emailVerificationToken));
    log.trace("registerUser() completed");
  }

  private static boolean isUserIdentityConflict(final DuplicateKeyException exception) {
    final String message = exception.getMostSpecificCause().getMessage();
    if (message == null) {
      return false;
    }

    final boolean isUsernameConflict = message.contains(USERNAME_UNIQUE_CONSTRAINT);
    final boolean isEmailConflict = message.contains(EMAIL_UNIQUE_CONSTRAINT);
    return isUsernameConflict || isEmailConflict;
  }

  @Override
  public TokenPair loginUser(final Email email, final Password password) {
    log.trace("loginUser() started");
    final Optional<UserEntity> userDbOptional = userRepository.findByEmail(email);
    if (userDbOptional.isEmpty()) {
      // Prevent account enumeration by matching the BCrypt work performed for existing accounts.
      passwordEncoder.verifyDummyPassword(password.value());
      throw new BadCredentialsException("Invalid credentials");
    }
    final UserEntity userDb = userDbOptional.get();
    final boolean passwordMatches =
        passwordEncoder.verify(password.value(), userDb.hashedPassword());
    final boolean invalidCredentials =
        !userDb.isEnabled() || !userDb.isEmailVerified() || !passwordMatches;
    if (invalidCredentials) {
      throw new BadCredentialsException("Invalid credentials");
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

  @Transactional(noRollbackFor = RefreshTokenReuseDetectedException.class)
  @Override
  public TokenPair refreshTokens(final RefreshToken refreshToken) {
    log.trace("refreshTokens() started");
    // Find the refresh token in the database
    final Optional<RefreshTokenEntity> refreshTokenDbOptional =
        refreshTokenRepository.findByToken(refreshToken);
    if (refreshTokenDbOptional.isEmpty()) {
      // check if the refresh token has already been used (refresh token reuse detection)
      final Optional<UsedRefreshTokenEntity> usedRefreshToken =
          usedRefreshTokenRepository.findByToken(refreshToken);
      if (usedRefreshToken.isPresent()) {
        // reuse confirmed. blacklist all the family
        blacklistFamily(usedRefreshToken.get().family());
        throw new RefreshTokenReuseDetectedException();
      }
      throw new RefreshTokenNotFoundException("Refresh token not found");
    }
    final RefreshTokenEntity refreshTokenDb = refreshTokenDbOptional.get();
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
    // add the token to the list of used refresh tokens
    final Optional<UsedRefreshTokenEntity> usedRefreshToken =
        usedRefreshTokenRepository.create(
            new CreateUsedRefreshTokenDto(
                refreshToken, refreshTokenDb.family(), Instant.now(clock)));
    if (usedRefreshToken.isEmpty()) {
      blacklistFamily(refreshTokenDb.family());
      throw new RefreshTokenReuseDetectedException();
    }
    // generate new tokens
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
    final Optional<UserEntity> user = userRepository.findByEmail(email);
    if (user.isEmpty()) {
      return;
    }
    final UserEntity userDb = user.get();
    if (userDb.isEmailVerified()) {
      return;
    }
    // check minimum wait time between requests. If the current instant is before the createdAt
    // plus the configured wait time, the user is requesting a new verification email too soon.
    final Optional<EmailVerificationEntity> oldEmailVerificationTokenDb =
        emailVerificationRepository.findByUserId(userDb.id());
    final Instant now = Instant.now(clock);
    final Instant nextRequestAt =
        oldEmailVerificationTokenDb
            .map(token -> token.lastSentAt().plus(emailVerificationProperties.waitTime()))
            .orElse(now);
    if (now.isBefore(nextRequestAt)) {
      throw new NewEmailVerificationTooSoonException(nextRequestAt);
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

  /**
   * Starts a password reset for an enabled account without revealing whether the email exists.
   *
   * @param email email address associated with the account
   * @throws DatabaseExecutionException when the password reset token cannot be persisted
   */
  @Transactional
  @Override
  public void forgotPassword(final Email email) {
    final Optional<UserEntity> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty() || !userOptional.get().isEnabled()) {
      return;
    }

    final UserEntity user = userOptional.get();
    final Instant now = Instant.now(clock);
    final Optional<PasswordResetEntity> existingToken =
        passwordResetRepository.findByUserId(user.id());
    final boolean requestIsRateLimited =
        existingToken
            .map(token -> now.isBefore(token.lastSentAt().plus(resetPasswordProperties.waitTime())))
            .orElse(false);
    if (requestIsRateLimited) {
      return;
    }

    final PasswordResetToken resetToken = PasswordResetToken.generate();
    final ExpirationTime expiresAt =
        ExpirationTime.of(now.plus(resetPasswordProperties.tokenDuration()));
    if (existingToken.isEmpty()) {
      passwordResetRepository
          .create(new CreatePasswordResetDto(user.id(), resetToken, expiresAt, now))
          .orElseThrow(
              () -> new DatabaseExecutionException("Could not create password reset token"));
    } else if (!passwordResetRepository.updateByUserId(
        user.id(), new UpdatePasswordResetDto(resetToken, expiresAt, now))) {
      throw new DatabaseExecutionException("Could not update password reset token");
    }

    eventPublisher.publishEvent(
        new PasswordResetRequestedEvent(user.id(), user.email(), user.username(), resetToken));
  }

  /**
   * Resets an account password using a valid, unexpired password reset token.
   *
   * @param token password reset token presented by the user
   * @param newPassword replacement password
   * @throws ResetPasswordTokenNotFoundException when the token is invalid or already used
   * @throws ResetPasswordTokenExpiredException when the token has expired
   * @throws DatabaseExecutionException when the password or token cannot be persisted
   */
  @Transactional
  @Override
  public void resetPassword(final PasswordResetToken token, final Password newPassword) {
    final Instant now = Instant.now(clock);
    final PasswordResetEntity resetToken =
        passwordResetRepository
            .findByToken(token)
            .orElseThrow(
                () -> new ResetPasswordTokenNotFoundException("Password reset token not found"));
    if (!resetToken.expiresAt().value().isAfter(now)) {
      throw new ResetPasswordTokenExpiredException("Password reset token expired");
    }

    final boolean passwordUpdated =
        userRepository.updateById(
            resetToken.userId(),
            UpdateUserDto.builder()
                .hashedPassword(passwordEncoder.encode(newPassword.value()))
                .build());
    if (!passwordUpdated) {
      throw new DatabaseExecutionException("Could not update the user password in the database");
    }

    final boolean tokenDeleted = passwordResetRepository.deleteByUserId(resetToken.userId());
    if (!tokenDeleted) {
      throw new DatabaseExecutionException("Could not delete password reset token");
    }

    refreshTokenRepository.deleteByUserId(resetToken.userId());
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
    blacklistFamily(refreshTokenDb.family());
  }

  @Override
  public boolean isFamilyBlacklisted(final TokenFamily family) {
    return tokenFamilyBlacklistRepository.exists(family);
  }

  private void blacklistFamily(final TokenFamily family) {
    tokenFamilyBlacklistRepository.create(new CreateTokenFamilyBlacklistDto(family));
  }
}
