package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.CreateGroupResponseDto;
import com.familymoney.familymoney.controllers.dtos.grouptransaction.GetTransactionsResponseDto;
import com.familymoney.familymoney.services.data.TransactionData;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetGroupTransactionsResponseMapper {

  public GetTransactionsResponseDto toDto(List<TransactionData> transactionList) {
    return GetTransactionsResponseDto.builder().id(groupId.value()).build();
  }
}
