package com.familymoney.familymoney.controllers.dtos.user;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.UserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserRequestDto(
        @Nullable UserName username, @Nullable Email email, @Nullable Password password) {}
