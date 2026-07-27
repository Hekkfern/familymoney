package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;

public record TransactionEntity(
    TransactionId id,
    Description description,
    GroupId groupId,
    Money amount,
    UserId from,
    UserId to,
    Instant doneAt,
    Instant createdAt,
    Instant updatedAt) {}
