package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.UserName;

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
