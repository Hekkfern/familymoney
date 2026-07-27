package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.UserName;

public interface IAuthService {

  void registerUser(UserName username, Email email, Password password);

  TokenPair loginUser(Email email, Password password);

  TokenPair refreshTokens(RefreshToken refreshToken);

  void verifyEmail(EmailVerificationToken token);

  void resendVerificationEmail(Email email);

  void forgotPassword(Email email);

  void resetPassword(PasswordResetToken token, Password newPassword);

  void logoutUser(RefreshToken refreshToken);

  boolean isFamilyBlacklisted(TokenFamily family);
}
