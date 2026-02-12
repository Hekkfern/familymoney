package com.familymoney.familymoney.repositories.entities;

import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import lombok.Builder;
import org.javamoney.moneta.Money;

@Builder
public record BalanceEntity(BalanceId id, GroupId groupId, Money amount, UserId user1, UserId user2) {}
