package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.user.types.UserId;
import lombok.Builder;
import org.javamoney.moneta.Money;

@Builder
public record BalanceEntity(
    BalanceId id, GroupId groupId, Money amount, UserId user1, UserId user2) {}
