package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import org.javamoney.moneta.Money;

public record BalanceEntity(GroupId groupId, Money money, UserId user1, UserId user2) {}
