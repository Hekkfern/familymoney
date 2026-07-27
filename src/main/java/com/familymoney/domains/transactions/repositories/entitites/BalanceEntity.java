package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import org.javamoney.moneta.Money;

public record BalanceEntity(
    BalanceId id, GroupId groupId, Money money, UserId user1, UserId user2) {}
