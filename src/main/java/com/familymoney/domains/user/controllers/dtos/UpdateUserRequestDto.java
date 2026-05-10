package com.familymoney.domains.user.controllers.dtos;

import com.familymoney.domains.user.validation.ValidEmail;
import com.familymoney.domains.user.validation.ValidPassword;
import com.familymoney.domains.user.validation.ValidUserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserRequestDto(
    @Nullable @ValidUserName String username,
    @Nullable @ValidEmail String email,
    @Nullable @ValidPassword String password) {}
