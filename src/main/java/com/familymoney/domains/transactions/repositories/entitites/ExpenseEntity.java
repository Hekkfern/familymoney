package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import java.time.Instant;

public record ExpenseEntity(
    ExpenseId id, Description description, GroupId groupId, Instant doneAt) {}
