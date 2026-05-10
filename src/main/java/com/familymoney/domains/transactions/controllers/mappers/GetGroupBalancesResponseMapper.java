package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.controllers.dtos.grouptransaction.GetGroupBalancesResponseDto;
import com.familymoney.domains.user.types.UserId;
import java.util.Map;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;

@Component
public class GetGroupBalancesResponseMapper {

  public GetGroupBalancesResponseDto toDto(Map<UserId, Money> balanceMap) {
    var balances =
        balanceMap.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().value(), Map.Entry::getValue));
    return GetGroupBalancesResponseDto.builder().balances(balances).build();
  }
}
