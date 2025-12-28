package com.familymoney.familymoney.controllers.dtos.user;

import com.familymoney.familymoney.validation.ValidEmail;
import com.familymoney.familymoney.validation.ValidPassword;
import com.familymoney.familymoney.validation.ValidUserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserRequestDto(
    @Nullable @ValidUserName String username,
    @Nullable @ValidEmail String email,
    @Nullable @ValidPassword String password) {}
