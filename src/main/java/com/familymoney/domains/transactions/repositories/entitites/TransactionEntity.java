package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;

@Builder
public record TransactionEntity(
    TransactionId id,
    String description,
    GroupId groupId,
    Money amount,
    UserId from,
    UserId to,
    Instant doneAt,
    Instant createdAt,
    Instant updatedAt) {}
