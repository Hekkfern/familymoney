package com.familymoney.domains.transactions.repositories.entitites;

import java.util.List;

public record FullExpenseEntity(
    ExpenseEntity expense, List<ExpenseShareEntity> shares, List<ExpensePaymentEntity> payments) {}
