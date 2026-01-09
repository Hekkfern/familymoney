package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.GetInvitationTokenResponseDto;
import com.familymoney.familymoney.types.GroupInvitationToken;
import org.springframework.stereotype.Component;

@Component
public class GetInvitationTokenResponseMapper {

  public GetInvitationTokenResponseDto toDto(GroupInvitationToken token) {
    return GetInvitationTokenResponseDto.builder().token(token.value()).build();
  }
}
