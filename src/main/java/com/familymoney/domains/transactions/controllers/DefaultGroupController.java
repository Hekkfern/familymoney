package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.InvitationTokenDto;
import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.BalanceDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GroupDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.MemberDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.TransactionDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateGroupRequestMapper;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.BalanceData;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import com.familymoney.utils.PageResponseMapper;
import io.github.benhendayoussef.idempotency.api.Idempotent;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultGroupController implements GroupController {

  private final GroupService groupService;

  @Override
  public CreateGroupResponseDto createGroupAndAddCreatorAsMember(final CreateGroupRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final GroupId groupId =
        groupService.createGroupAndAddCreatorAsMember(
            GroupName.fromString(request.name()),
            Description.of(request.description().trim()),
            Currency.getInstance(request.currencyCode()),
            user);
    return CreateGroupResponseMapper.toDto(groupId);
  }

  @Override
  public PageResponse<GroupDto> getGroupsForUser(final int page, final int size) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final Page<GroupData> groupPages =
        groupService.getGroupsForUser(user.id(), PageRequest.of(page, size));
    return PageResponseMapper.from(groupPages.map(GroupDtoMapper::toDto));
  }

  @Override
  public void leaveGroup(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.leaveGroup(GroupId.fromUuid(groupId), user);
  }

  @Override
  public GroupDto getGroupInfo(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final GroupData groupData = groupService.getGroupInfo(GroupId.fromUuid(groupId), user);
    return GroupDtoMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(final UUID groupId, final UpdateGroupRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.updateGroupInfo(
        GroupId.fromUuid(groupId), user, UpdateGroupRequestMapper.fromDto(request));
  }

  @Override
  public InvitationTokenDto getInvitationToken(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final GroupInvitationToken token =
        groupService.getInvitationToken(GroupId.fromUuid(groupId), user);
    return new InvitationTokenDto(token.value());
  }

  @Override
  public void enterToGroup(final EnterGroupRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.enterToGroupWithToken(GroupInvitationToken.fromString(request.token()), user);
  }

  @Override
  public List<MemberDto> getUsersInGroup(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final List<UserData> users = groupService.getUsersInGroup(GroupId.fromUuid(groupId), user);
    return users.stream().map(MemberDtoMapper::toDto).toList();
  }

  @Override
  public List<BalanceDto> getGroupBalances(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final List<BalanceData> balances =
        groupService.getGroupBalances(GroupId.fromUuid(groupId), user);
    return balances.stream().map(BalanceDtoMapper::toDto).toList();
  }

  @Override
  public PageResponse<TransactionDto> getGroupTransactions(final UUID groupId, int page, int size) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final Page<TransactionData> transactionPages =
        groupService.getGroupTransactions(
            GroupId.fromUuid(groupId), user, PageRequest.of(page, size));
    return PageResponseMapper.from(transactionPages.map(TransactionDtoMapper::toDto));
  }
}
