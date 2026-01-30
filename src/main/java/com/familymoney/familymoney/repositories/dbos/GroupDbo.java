package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import javax.money.CurrencyUnit;
import lombok.Builder;

@Builder
public record GroupDbo(
    GroupId id,
    GroupName name,
    String description,
    CurrencyUnit currency,
    Instant createdAt,
    Instant updatedAt) {}
