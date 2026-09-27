package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;

public record BalanceKeyDto(GroupId groupId, UserId user1, UserId user2) {}
