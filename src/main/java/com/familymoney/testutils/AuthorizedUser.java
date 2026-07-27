package com.familymoney.testutils;

import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import lombok.Builder;

@Builder
public record AuthorizedUser(UserId id, Role role) {}
