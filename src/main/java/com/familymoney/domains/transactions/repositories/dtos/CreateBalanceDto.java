package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import org.javamoney.moneta.Money;

public record CreateBalanceDto(
    BalanceId id, GroupId groupId, UserId user1, UserId user2, Money amount) {}
