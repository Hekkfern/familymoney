package com.familymoney.familymoney.utils;

import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.UserId;
import lombok.Builder;

@Builder
public record AuthorizedUser(UserId id, Role role) {}
