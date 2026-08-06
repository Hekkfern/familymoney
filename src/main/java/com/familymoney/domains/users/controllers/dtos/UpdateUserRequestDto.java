package com.familymoney.domains.users.controllers.dtos;

import com.familymoney.domains.users.validations.ValidEmail;
import com.familymoney.domains.users.validations.ValidPassword;
import com.familymoney.domains.users.validations.ValidUserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserRequestDto(
    @Nullable @ValidUserName String username,
    @Nullable @ValidEmail String email,
    @Nullable @ValidPassword String password) {}
