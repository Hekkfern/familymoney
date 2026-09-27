package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;

public record ExpensePaymentEntity(UserId userId, BigDecimal amount) {}
