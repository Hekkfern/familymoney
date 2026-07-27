package com.familymoney.domains.users.controllers.dtos;

import com.familymoney.domains.users.validation.ValidEmail;
import com.familymoney.domains.users.validation.ValidPassword;
import com.familymoney.domains.users.validation.ValidUserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserRequestDto(
    @Nullable @ValidUserName String username,
    @Nullable @ValidEmail String email,
    @Nullable @ValidPassword String password) {}
