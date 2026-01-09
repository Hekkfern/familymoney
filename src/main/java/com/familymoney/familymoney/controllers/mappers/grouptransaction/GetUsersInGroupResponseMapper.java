package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.GetUsersInGroupResponseDto;
import com.familymoney.familymoney.types.UserId;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class GetUsersInGroupResponseMapper {

  public GetUsersInGroupResponseDto toDto(List<UserId> users) {
    val userIds = users.stream().map(UserId::value).toList();
    return GetUsersInGroupResponseDto.builder().userIds(userIds).build();
  }
}
