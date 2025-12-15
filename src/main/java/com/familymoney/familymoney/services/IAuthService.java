package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.TokenPair;
import com.familymoney.familymoney.types.*;

public interface IAuthService {

  void registerUser(Username username, Email email, Password password);

  TokenPair loginUser(Email email, Password password);

  TokenPair refreshTokens(RefreshToken refreshToken);

  void verifyEmail(EmailVerificationToken token);

  void resendVerificationEmail(Email email);

  void forgotPassword(Email email);

  void resetPassword(PasswordResetToken token, Password newPassword);

  void logoutUser(RefreshToken refreshToken);
}
