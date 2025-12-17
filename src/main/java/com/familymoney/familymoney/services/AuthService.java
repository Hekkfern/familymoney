package com.familymoney.familymoney.services;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.EmailNotFoundException;
import com.familymoney.familymoney.exceptions.RefreshTokenInvalidException;
import com.familymoney.familymoney.exceptions.RefreshTokenNotFoundException;
import com.familymoney.familymoney.exceptions.UserAlreadyExistsException;
import com.familymoney.familymoney.exceptions.VerificationTokenExpiredException;
import com.familymoney.familymoney.exceptions.VerificationTokenNotFoundException;
import com.familymoney.familymoney.repositories.IEmailVerificationRepository;
import com.familymoney.familymoney.repositories.IPasswordResetRepository;
import com.familymoney.familymoney.repositories.IRefreshTokenRepository;
import com.familymoney.familymoney.repositories.IRoleRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.services.data.TokenPair;
import com.familymoney.familymoney.types.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

  private static final Duration RESET_TOKEN_EXPIRY = Duration.ofHours(1);

  private final IUserRepository userRepository;
  private final UserPasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final IEmailSenderService emailSenderService;
  private final IRefreshTokenRepository refreshTokenRepository;
  private final IEmailVerificationRepository emailVerificationRepository;
  private final IPasswordResetRepository passwordResetRepository;
  private final IRoleRepository roleRepository;

  /**
   * Generate and store email verification token in the database, retrying on collision
   *
   * @param userId Identifier of the user to generate the token for
   * @return The stored email verification token database object
   */
  private EmailVerificationDbo generateAndStoreEmailVerificationToken(UserId userId) {
    final int MAX_NUM_ATTEMPTS = 3;
    final Duration VERIFICATION_TOKEN_EXPIRY = Duration.ofHours(24);

    val expiresAt = Instant.now().plus(VERIFICATION_TOKEN_EXPIRY);
    for (int attempt = 0; attempt < MAX_NUM_ATTEMPTS; attempt++) {
      val token = EmailVerificationToken.generate();
      val storedOpt = emailVerificationRepository.create(userId, token, expiresAt);
      if (storedOpt.isPresent()) {
        return storedOpt.get();
      }
      log.warn(
          "Token collision detected when generating email verification token for userId {}. Retrying...",
          userId);
    }
    throw new DatabaseExecutionException(
        "Could not generate a unique email verification token after multiple attempts");
  }

  @Transactional
  @Override
  public void registerUser(Username username, Email email, Password password) {
    log.trace("registerUser() started");
    // Check if user already exists
    if (userRepository.existsByEmailOrUsername(email, username)) {
      throw new UserAlreadyExistsException("A user with that email already exists.");
    }
    // Create user
    val userDb =
        userRepository
            .create(username, email, passwordEncoder.encode(password.value()))
            .orElseThrow(
                () -> new DatabaseExecutionException("Could not create user in the database"));
    // Assign user permissions (default role)
    roleRepository.setRoleForUserId(userDb.id(), Role.USER);
    // Generate and save verification token to database
    // NOTE: Retry several times in case of token collision
    val emailVerificationTokenDb = generateAndStoreEmailVerificationToken(userDb.id());
    // Send verification email
    emailSenderService.sendEmailVerificationEmail(
        email, username, emailVerificationTokenDb.token());
    log.trace("registerUser() completed");
  }

  @Override
  public TokenPair loginUser(Email email, Password password) {
    log.trace("loginUser() started");
    // Find user by email
    val userDb =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Email doesn't exist"));
    // Verify that user is enabled
    if (!userDb.isEnabled()) {
      throw new BadCredentialsException("User is not enabled");
    }
    // Verify that email is verified
    if (!userDb.emailVerified()) {
      throw new BadCredentialsException("Email is not verified");
    }
    // Verify password
    if (!passwordEncoder.verify(password.value(), userDb.hashedPassword())) {
      throw new BadCredentialsException("Wrong password for the given email");
    }
    // Generate access token
    val accessToken = jwtUtil.generateAccessToken(userDb.id());
    // Generate refresh token
    val refreshToken = RefreshToken.generate();
    // Save refresh token in database
    refreshTokenRepository
        .create(userDb.id(), refreshToken, UUID.randomUUID())
        .orElseThrow(
            () -> new DatabaseExecutionException("Could not create refresh token in the database"));
    // Build response with both tokens
    log.trace("loginUser() finished");
    return new TokenPair(accessToken, refreshToken);
  }

  @Override
  public TokenPair refreshTokens(RefreshToken refreshToken) {
    log.trace("refreshTokens() started");
    // Find the refresh token in the database
    val refreshTokenDb =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));
    // Check if the refresh token is expired
    if (refreshTokenDb.isExpired()) {
      val msg = "Invalid refresh token";
      log.info(msg);
      throw new RefreshTokenInvalidException(msg);
    }
    // Check if the token was already used
    if (refreshTokenDb.isUsed()) {
      // Invalidate all refresh tokens for that user
      log.warn("REFRESH TOKEN REUSE DETECTED!");
      refreshTokenRepository.invalidateByFamily(refreshTokenDb.family());
      // Get user info for email
      val userDb =
          userRepository
              .findById(refreshTokenDb.userId())
              .orElseThrow(() -> new DatabaseExecutionException("User not found in the database"));
      // Send security alert email
      emailSenderService.sendSecurityAlertEmail(userDb.email(), userDb.username());
      // Throw exception
      throw new RefreshTokenInvalidException("Refresh token not found in the database");
    }
    // Mark the old token as used
    refreshTokenRepository.markTokenAsUsed(refreshTokenDb.token());
    // Generate new tokens
    val newAccessToken = jwtUtil.generateAccessToken(refreshTokenDb.userId());
    val newRefreshToken = RefreshToken.generate();
    // Save new refresh token in database
    refreshTokenRepository
        .create(refreshTokenDb.userId(), newRefreshToken, refreshTokenDb.family())
        .orElseThrow(
            () ->
                new DatabaseExecutionException(
                    "Could not create new refresh token in the database"));
    // Build response with both tokens
    log.trace("refreshTokens() finished");
    return new TokenPair(newAccessToken, newRefreshToken);
  }

  @Override
  public void verifyEmail(EmailVerificationToken token) {
    // Find the verification token in the database
    val verificationTokenDb =
        emailVerificationRepository
            .findByToken(token)
            .orElseThrow(
                () -> new VerificationTokenNotFoundException("Invalid email verification token"));
    // Check if the token is expired
    if (verificationTokenDb.isExpired()) {
      throw new VerificationTokenExpiredException("Email verification token has expired");
    }
    // Verify the user's email
    userRepository.verifyEmail(verificationTokenDb.userId());
    // Delete all the verification tokens assigned to this user from the database
    emailVerificationRepository.deleteByUserId(verificationTokenDb.userId());
  }

  @Override
  public void resendVerificationEmail(Email email) {
    // Look for user by email
    val userDb =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmailNotFoundException("User with that email not found"));
    // Generate and save verification token to database
    // NOTE: Retry several times in case of token collision
    val emailVerificationTokenDb = generateAndStoreEmailVerificationToken(userDb.id());
    // Send verification email
    emailSenderService.sendEmailVerificationEmail(
        email, userDb.username(), emailVerificationTokenDb.token());
  }

  @Override
  public void forgotPassword(Email email) {
    // TODO
  }

  @Override
  public void resetPassword(PasswordResetToken token, Password newPassword) {
    // TODO
  }

  @Override
  public void logoutUser(RefreshToken refreshToken) {
    // Find the refresh token in the database
    val refreshTokenDb =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(
                () -> new RefreshTokenNotFoundException("Refresh token not found in the database"));
    // Check if the refresh token is valid
    if (!refreshTokenDb.isValid()) {
      val msg = "Invalid refresh token";
      log.trace(msg);
      throw new RefreshTokenInvalidException(msg);
    }
    // Invalidate the family of refresh token from the database
    refreshTokenRepository.invalidateByFamily(refreshTokenDb.family());
  }
}
