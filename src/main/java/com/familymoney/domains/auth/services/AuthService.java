package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.repositories.IEmailVerificationRepository;
import com.familymoney.domains.auth.repositories.IPasswordResetRepository;
import com.familymoney.domains.auth.repositories.IRefreshTokenRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.repositories.IRoleRepository;
import com.familymoney.domains.user.repositories.IUserRepository;
import com.familymoney.domains.user.repositories.dtos.CreateUserDto;
import com.familymoney.domains.user.repositories.dtos.UpdateUserDto;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.domains.auth.exceptions.EmailAlreadyVerifiedException;
import com.familymoney.domains.auth.exceptions.EmailNotFoundException;
import com.familymoney.domains.auth.exceptions.RefreshTokenInvalidException;
import com.familymoney.domains.auth.exceptions.RefreshTokenNotFoundException;
import com.familymoney.domains.auth.exceptions.UserAlreadyExistsException;
import com.familymoney.domains.auth.exceptions.VerificationTokenExpiredException;
import com.familymoney.domains.auth.exceptions.VerificationTokenNotFoundException;
import com.familymoney.security.JwtUtils;
import com.familymoney.security.UserPasswordEncoder;
import com.familymoney.utils.UUIDGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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

  private static final Duration VERIFICATION_TOKEN_EXPIRY = Duration.ofHours(24);

  private final IUserRepository userRepository;
  private final UserPasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final IEmailSenderService emailSenderService;
  private final IRefreshTokenRepository refreshTokenRepository;
  private final IEmailVerificationRepository emailVerificationRepository;
  private final IPasswordResetRepository passwordResetRepository;
  private final IRoleRepository roleRepository;
  private final Clock clock;

  @Transactional
  @Override
  public void registerUser(final UserName username, final Email email, final Password password) {
    log.trace("registerUser() started");
    // Check if user already exists
    if (userRepository.existsByEmailOrUsername(email, username)) {
      throw new UserAlreadyExistsException("A user with that email or username already exists.");
    }
    // Create user
    val userId = UserId.generate();
    userRepository
        .create(
            CreateUserDto.builder()
                .id(userId)
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password.value()))
                .build())
        .orElseThrow(() -> new DatabaseExecutionException("Could not create user in the database"));
    // Assign user permissions (default role)
    roleRepository.setRoleForUserId(userId, Role.USER);
    // Generate and save verification token to database
    val emailVerificationTokenDb =
        emailVerificationRepository
            .create(
                CreateEmailVerificationDto.builder()
                    .id(UUIDGenerator.generate())
                    .userId(userId)
                    .token(EmailVerificationToken.generate())
                    .expiresAt(Instant.now(clock).plus(VERIFICATION_TOKEN_EXPIRY))
                    .build())
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
    if (!userDb.isEmailVerified()) {
      throw new BadCredentialsException("Email is not verified");
    }
    // Verify password
    if (!passwordEncoder.verify(password.value(), userDb.hashedPassword())) {
      throw new BadCredentialsException("Wrong password for the given email");
    }
    // Generate family ID for the tokens of this session
    val family = TokenFamily.generate();
    // Generate access token
    val accessToken = jwtUtils.generateAccessToken(userDb.id(), family);
    // Generate refresh token
    val refreshToken = RefreshToken.generate();
    // Save refresh token in database
    refreshTokenRepository
        .create(
            CreateRefreshTokenDto.builder()
                .id(UUIDGenerator.generate())
                .userId(userDb.id())
                .token(refreshToken)
                .family(family)
                .build())
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
    if (Instant.now(clock).isAfter(refreshTokenDb.expiresAt())) {
      val msg = "Invalid refresh token";
      log.info(msg);
      throw new RefreshTokenInvalidException(msg);
    }
    // Check if the token was already used
    if (refreshTokenDb.isUsed()) {
      // Invalidate all refresh tokens for that user
      log.warn("REFRESH TOKEN REUSE DETECTED!");
      refreshTokenRepository.updateByFamily(
          refreshTokenDb.family(), UpdateRefreshTokenDto.builder().isUsed(true).build());
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
    refreshTokenRepository.updateByToken(
        refreshTokenDb.token(),
        UpdateRefreshTokenDto.builder().isUsed(true).usedAt(Instant.now(clock)).build());
    // Generate new tokens
    val newAccessToken = jwtUtils.generateAccessToken(refreshTokenDb.userId());
    val newRefreshToken = RefreshToken.generate();
    // Save new refresh token in database
    val refreshTokenId = UUIDGenerator.generate();
    refreshTokenRepository
        .create(
            CreateRefreshTokenDto.builder()
                .id(refreshTokenId)
                .userId(refreshTokenDb.userId())
                .token(newRefreshToken)
                .family(refreshTokenDb.family())
                .build())
        .orElseThrow(
            () ->
                new DatabaseExecutionException(
                    "Could not create new refresh token in the database"));
    // Build response with both tokens
    log.trace("refreshTokens() finished");
    return new TokenPair(newAccessToken, newRefreshToken);
  }

  @Override
  public void verifyEmail(final EmailVerificationToken token) {
    // Find the verification token in the database
    val emailVerificationTokenDb =
        emailVerificationRepository
            .findByToken(token)
            .orElseThrow(
                () -> new VerificationTokenNotFoundException("Invalid email verification token"));
    // Check if the token is expired
    if (Instant.now(clock).isAfter(emailVerificationTokenDb.expiresAt())) {
      throw new VerificationTokenExpiredException("Email verification token has expired");
    }
    // Verify the user's email
    userRepository.updateById(
        emailVerificationTokenDb.userId(), UpdateUserDto.builder().isEmailVerified(true).build());
    // Delete all the verification tokens assigned to this user from the database
    emailVerificationRepository.deleteByUserId(emailVerificationTokenDb.userId());
  }

  @Override
  public void resendVerificationEmail(final Email email) {
    // Look for user by email
    val userDb =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmailNotFoundException("User with that email not found"));
    // Check if user is already verified. If so, skip
    if (userDb.isEmailVerified()) {
        throw new EmailAlreadyVerifiedException("Email is already verified");
    }


    // Generate and save verification token to database
    val newEmailVerificationToken = EmailVerificationToken.generate();
    emailVerificationRepository
        .updateById(
            UpdateEmailVerificationTokenDto.builder()
                .token(EmailVerificationToken.generate())
                .expiresAt(Instant.now(clock).plus(VERIFICATION_TOKEN_EXPIRY))
                .build())
        .orElseThrow(
            () ->
                new DatabaseExecutionException(
                    "Could not create email verification token in the database"));
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
    if (Instant.now(clock).isAfter(refreshTokenDb.expiresAt()) || refreshTokenDb.isUsed()) {
      val msg = "Invalid refresh token";
      log.trace(msg);
      throw new RefreshTokenInvalidException(msg);
    }
    // Invalidate the family of refresh token from the database
    refreshTokenRepository.updateByFamily(
        refreshTokenDb.family(), UpdateRefreshTokenDto.builder().isUsed(true).build());
  }
}
