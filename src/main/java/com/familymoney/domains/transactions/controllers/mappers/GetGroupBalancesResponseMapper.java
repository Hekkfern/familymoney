package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetGroupBalancesResponseDto;
import com.familymoney.domains.users.types.UserId;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;

public final class GetGroupBalancesResponseMapper {

  private GetGroupBalancesResponseMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GetGroupBalancesResponseDto toDto(final Map<UserId, Money> balanceMap) {
    final Map<UUID, Money> balances =
        balanceMap.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().value(), Map.Entry::getValue));
    return new GetGroupBalancesResponseDto(balances);
  }
}
