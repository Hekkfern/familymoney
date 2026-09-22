package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;

public record ExpensePaymentEntity(ExpenseId id, UserId userId, BigDecimal amount) {}
