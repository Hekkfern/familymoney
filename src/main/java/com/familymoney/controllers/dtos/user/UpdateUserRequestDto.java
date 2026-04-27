package com.familymoney.controllers.dtos.user;

import com.familymoney.validation.ValidEmail;
import com.familymoney.validation.ValidPassword;
import com.familymoney.validation.ValidUserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserRequestDto(
    @Nullable @ValidUserName String username,
    @Nullable @ValidEmail String email,
    @Nullable @ValidPassword String password) {}
