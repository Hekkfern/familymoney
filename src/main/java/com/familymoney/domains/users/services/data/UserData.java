package com.familymoney.domains.users.services.data;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;

public record UserData(
    UserId id, UserName username, Email email, boolean isEmailVerified, boolean isEnabled) {}
