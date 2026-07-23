package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupName;
import java.time.Instant;
import javax.money.CurrencyUnit;

public record CreateGroupData(
    GroupName name, Description description, CurrencyUnit currency, Instant createdAt) {}
