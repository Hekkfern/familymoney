package com.familymoney.familymoney.repositories.entities;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
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
