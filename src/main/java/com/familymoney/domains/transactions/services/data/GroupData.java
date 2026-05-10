package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import java.time.Instant;
import javax.money.CurrencyUnit;
import lombok.Builder;

@Builder
public record GroupData(
    GroupId id,
    GroupName name,
    String description,
    CurrencyUnit currency,
    Instant createdAt) {}
