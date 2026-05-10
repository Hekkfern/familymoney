package com.familymoney.utils;

import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import lombok.Builder;

@Builder
public record AuthorizedUser(UserId id, Role role) {}
