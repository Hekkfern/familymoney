package com.familymoney.repositories.entities;

import com.familymoney.types.BalanceId;
import com.familymoney.types.GroupId;
import com.familymoney.types.UserId;
import lombok.Builder;
import org.javamoney.moneta.Money;

@Builder
public record BalanceEntity(BalanceId id, GroupId groupId, Money amount, UserId user1, UserId user2) {}
