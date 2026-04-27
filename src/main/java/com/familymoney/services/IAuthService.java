package com.familymoney.services;

import com.familymoney.services.data.TokenPair;
import com.familymoney.types.*;

public interface IAuthService {

  void registerUser(UserName username, Email email, Password password);

  TokenPair loginUser(Email email, Password password);

  TokenPair refreshTokens(RefreshToken refreshToken);

  void verifyEmail(EmailVerificationToken token);

  void resendVerificationEmail(Email email);

  void forgotPassword(Email email);

  void resetPassword(PasswordResetToken token, Password newPassword);

  void logoutUser(RefreshToken refreshToken);
}
