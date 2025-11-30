package com.familymoney.familymoney.services;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.EmailNotFoundException;
import com.familymoney.familymoney.exceptions.InvalidRefreshTokenException;
import com.familymoney.familymoney.exceptions.RefreshTokenNotFoundException;
import com.familymoney.familymoney.exceptions.UserAlreadyExistsException;
import com.familymoney.familymoney.exceptions.VerificationTokenExpiredException;
import com.familymoney.familymoney.exceptions.VerificationTokenNotFoundException;
import com.familymoney.familymoney.repositories.IEmailVerificationRepository;
import com.familymoney.familymoney.repositories.IPasswordResetRepository;
import com.familymoney.familymoney.repositories.IPermissionsRepository;
import com.familymoney.familymoney.repositories.IRefreshTokenRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

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
  private final IPermissionsRepository permissionsRepository;

  /**
   * Generate and store email verification token in the database, retrying on collision
   *
   * @param userId Identifier of the user to generate the token for
   * @return The stored email verification token database object
   */
  private EmailVerificationDbo generateAndStoreEmailVerificationToken(@NonNull UserId userId) {
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

  @Override
  public void registerUser(
      @NonNull Username username, @NonNull Email email, @NonNull Password password) {
    log.trace("registerUser() started");
    // Check if user already exists
    if (userRepository.existsByEmailOrUsername(email, username)) {
      throw new UserAlreadyExistsException("A user with that email already exists.");
    }
    // Create user
    val userDbOpt =
        userRepository.create(username, email, passwordEncoder.encode(password.value()));
    if (userDbOpt.isEmpty()) {
      throw new DatabaseExecutionException("Could not create user in the database");
    }
    val userDb = userDbOpt.get();
    // Assign user permissions (default role)
    permissionsRepository.setRoleForUserId(userDb.id(), "user");
    // Generate and save verification token to database
    // NOTE: Retry several times in case of token collision
    val emailVerificationTokenDb = generateAndStoreEmailVerificationToken(userDb.id());
    // Send verification email
    emailSenderService.sendEmailVerificationEmail(
        email, username, emailVerificationTokenDb.token());
    log.trace("registerUser() completed");
  }

  @Override
  @NonNull
  public TokenPair loginUser(@NonNull Email email, @NonNull Password password) {
    log.trace("loginUser() started");
    // Find user by email
    val userDbOpt = userRepository.findByEmail(email);
    if (userDbOpt.isEmpty()) {
      throw new BadCredentialsException("Email doesn't exist");
    }
    val userDb = userDbOpt.get();
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
    val refreshTokenDbOpt =
        refreshTokenRepository.create(userDb.id(), refreshToken, UUID.randomUUID());
    if (refreshTokenDbOpt.isEmpty()) {
      throw new DatabaseExecutionException("Could not create refresh token in the database");
    }
    // Build response with both tokens
    log.trace("loginUser() finished");
    return new TokenPair(accessToken, refreshToken);
  }

  @Override
  @NonNull
  public TokenPair refreshTokens(@NonNull RefreshToken refreshToken) {
    log.trace("refreshTokens() started");
    // Find the refresh token in the database
    val refreshTokenFoundInDbOpt = refreshTokenRepository.findByToken(refreshToken);
    if (refreshTokenFoundInDbOpt.isEmpty()) {
      throw new RefreshTokenNotFoundException("Refresh token not found");
    }
    val refreshTokenFoundInDb = refreshTokenFoundInDbOpt.get();
    // Check if the refresh token is expired
    if (refreshTokenFoundInDb.isExpired()) {
      val msg = "Invalid refresh token";
      log.trace(msg);
      throw new InvalidRefreshTokenException(msg);
    }
    // Check if the token was already used
    if (refreshTokenFoundInDb.isUsed()) {
      // Invalidate all refresh tokens for that user
      log.warn("REFRESH TOKEN REUSE DETECTED!");
      refreshTokenRepository.invalidateByFamily(refreshTokenFoundInDb.family());
      // Get user info for email
      val userDbOpt = userRepository.findById(refreshTokenFoundInDb.userId());
      if (userDbOpt.isEmpty()) {
        throw new DatabaseExecutionException("User not found in the database");
      }
      val userDb = userDbOpt.get();
      // Send security alert email
      emailSenderService.sendSecurityAlertEmail(userDb.email(), userDb.username());
      // Throw exception
      throw new InvalidRefreshTokenException("Refresh token not found in the database");
    }
    // Mark the old token as used
    refreshTokenRepository.markTokenAsUsed(refreshTokenFoundInDb.token());
    // Generate new tokens
    val newAccessToken = jwtUtil.generateAccessToken(refreshTokenFoundInDb.userId());
    val newRefreshToken = RefreshToken.generate();
    // Save new refresh token in database
    val refreshTokenCreatedInDbOpt =
        refreshTokenRepository.create(
            refreshTokenFoundInDb.userId(), newRefreshToken, refreshTokenFoundInDb.family());
    if (refreshTokenCreatedInDbOpt.isEmpty()) {
      throw new DatabaseExecutionException("Could not create new refresh token in the database");
    }
    // Build response with both tokens
    log.trace("refreshTokens() finished");
    return new TokenPair(newAccessToken, newRefreshToken);
  }

  @Override
  public void verifyEmail(@NonNull EmailVerificationToken token) {
    // Find the verification token in the database
    val verificationTokenFromDbOpt = emailVerificationRepository.findByToken(token);
    if (verificationTokenFromDbOpt.isEmpty()) {
      throw new VerificationTokenNotFoundException("Invalid email verification token");
    }
    val verificationTokenFromDb = verificationTokenFromDbOpt.get();
    // Check if the token is expired
    if (verificationTokenFromDb.isExpired()) {
      throw new VerificationTokenExpiredException("Email verification token has expired");
    }
    // Verify the user's email
    emailVerificationRepository.verifyEmail(verificationTokenFromDb.userId());
    // Delete all the verification tokens assigned to this user from the database
    emailVerificationRepository.deleteByUserId(verificationTokenFromDb.userId());
  }

  @Override
  public void resendVerificationEmail(@NonNull Email email) {
    // Look for user by email
    val userFromDbOpt = userRepository.findByEmail(email);
    if (userFromDbOpt.isEmpty()) {
      throw new EmailNotFoundException("User with that email not found");
    }
    val userFromDb = userFromDbOpt.get();
    // Generate and save verification token to database
    // NOTE: Retry several times in case of token collision
    val emailVerificationTokenDb = generateAndStoreEmailVerificationToken(userFromDb.id());
    // Send verification email
    emailSenderService.sendEmailVerificationEmail(
        email, userFromDb.username(), emailVerificationTokenDb.token());
  }

  @Override
  public void forgotPassword(@NonNull Email email) {
    // TODO
  }

  @Override
  public void resetPassword(@NonNull EmailVerificationToken token, @NonNull Password newPassword) {
    // TODO
  }

  @Override
  public void logoutUser(@NonNull RefreshToken refreshToken) {
    // Find the refresh token in the database
    val refreshTokenFromDbOpt = refreshTokenRepository.findByToken(refreshToken);
    if (refreshTokenFromDbOpt.isEmpty()) {
      throw new RefreshTokenNotFoundException("Refresh token not found in the database");
    }
    val refreshTokenFromDb = refreshTokenFromDbOpt.get();
    // Check if the refresh token is valid
    if (refreshTokenFromDb.isValid()) {
      val msg = "Invalid refresh token";
      log.trace(msg);
      throw new InvalidRefreshTokenException(msg);
    }
    // Invalidate the family of refresh token from the database
    refreshTokenRepository.invalidateByFamily(refreshTokenFromDb.family());
  }
}
