package com.familymoney.domains.transactions.services.data;

import java.util.UUID;

public record TransactionData(UUID id, TransactionType type) {}
