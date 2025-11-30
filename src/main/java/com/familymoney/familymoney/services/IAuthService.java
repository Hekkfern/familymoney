package com.familymoney.familymoney.services;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.Username;
import org.jspecify.annotations.NonNull;

public interface IAuthService {

  void registerUser(@NonNull Username username, @NonNull Email email, @NonNull Password password);

  @NonNull
  TokenPair loginUser(@NonNull Email email, @NonNull Password password);

  @NonNull
  TokenPair refreshTokens(@NonNull RefreshToken refreshToken);

  void verifyEmail(@NonNull EmailVerificationToken token);

  void resendVerificationEmail(@NonNull Email email);

  void forgotPassword(@NonNull Email email);

  void resetPassword(@NonNull EmailVerificationToken token, @NonNull Password newPassword);

  void logoutUser(@NonNull RefreshToken refreshToken);
}
