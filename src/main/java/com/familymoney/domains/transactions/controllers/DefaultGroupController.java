package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.idempotency.services.IdempotencyService;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.InvitationTokenDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GroupDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateGroupRequestMapper;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultGroupController implements GroupController {

  private final GroupService groupService;
  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;

  @Override
  public CreateGroupResponseDto createGroup(
      final String idempotencyKey,
      final CreateGroupRequestDto request,
      final HttpServletRequest httpRequest) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final IdempotencyKey key = IdempotencyKey.fromString(idempotencyKey);
    return idempotencyService.runWithIdempotency(
        key,
        user.id(),
        httpRequest,
        request,
        objectMapper.constructType(CreateGroupResponseDto.class),
        HttpStatus.OK,
        () -> {
          final GroupId groupId =
              groupService.createGroupAndAddCreatorAsMember(
                  GroupName.fromString(request.name()),
                  Description.of(request.description().trim()),
                  Monetary.getCurrency(request.currencyCode()),
                  user.id());
          return CreateGroupResponseMapper.toDto(groupId);
        });
  }

  @Override
  public List<UUID> getGroupsForUser() {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final Page<GroupData> groupPages =
        groupService.getGroupsByUser(user.id(), PageRequest.of(0, 10_000));
    return groupPages.getContent().stream().map(GroupDtoMapper::toDto).map(GroupDto::id).toList();
  }

  @Override
  public void deleteGroup(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.deleteGroup(GroupId.fromUuid(groupId), user.id());
  }

  @Override
  public GroupDto getGroupInfo(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final GroupData groupData = groupService.getGroupInfo(GroupId.fromUuid(groupId), user.id());
    return GroupDtoMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(final UUID groupId, final UpdateGroupRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.updateGroupInfo(
        GroupId.fromUuid(groupId), user.id(), UpdateGroupRequestMapper.fromDto(request));
  }

  @Override
  public InvitationTokenDto getInvitationToken(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final GroupInvitationToken token =
        groupService.getInvitationToken(GroupId.fromUuid(groupId), user.id());
    return new InvitationTokenDto(token.value());
  }

  @Override
  public void enterToGroup(final EnterGroupRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.enterToGroupWithToken(GroupInvitationToken.fromString(request.token()), user.id());
  }

  @Override
  public List<UUID> getUsersInGroup(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final List<UserId> users = groupService.getUsersInGroup(GroupId.fromUuid(groupId), user.id());
    return users.stream().map(UserId::value).toList();
  }

  @Override
  public void removeUserFromGroup(final UUID groupId, final UUID userId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.removeUserFromGroup(GroupId.fromUuid(groupId), user.id(), UserId.fromUuid(userId));
  }

  @Override
  public List<BalanceDto> getGroupBalances(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // TODO
    return null;
  }

  @Override
  public PageResponse<TransactionDto> getGroupTransactions(final UUID groupId, int page, int size) {
    // TODO
    return null;
  }
}
