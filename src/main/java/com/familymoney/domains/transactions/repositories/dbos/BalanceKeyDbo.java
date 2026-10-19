package com.familymoney.domains.transactions.repositories.dbos;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;

public record BalanceKeyDbo(GroupId groupId, UserId user1, UserId user2) {}
