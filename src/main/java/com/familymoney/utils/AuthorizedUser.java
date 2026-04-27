package com.familymoney.utils;

import com.familymoney.types.Role;
import com.familymoney.types.UserId;
import lombok.Builder;

@Builder
public record AuthorizedUser(UserId id, Role role) {}
