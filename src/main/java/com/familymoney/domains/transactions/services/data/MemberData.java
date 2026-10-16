package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;

public record MemberData(UserId userId, UserName username) {}
