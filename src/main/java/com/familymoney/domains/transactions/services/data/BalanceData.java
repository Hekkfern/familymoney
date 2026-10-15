package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.users.types.UserId;
import org.javamoney.moneta.Money;

public record BalanceData(UserId creditor, UserId debitor, Money amount) {}
