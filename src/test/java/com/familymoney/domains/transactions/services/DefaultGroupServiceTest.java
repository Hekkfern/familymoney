package com.familymoney.domains.transactions.services;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.events.GroupInvitationRequestedEvent;
import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupMembersReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupsPerUserReachedException;
import com.familymoney.domains.transactions.exceptions.SendGroupInvitationTooSoonException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInGroupException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInvitedException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.exceptions.WrongUserForInvitationException;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dbos.CreateGroupDbo;
import com.familymoney.domains.transactions.repositories.dbos.CreateGroupInvitationDbo;
import com.familymoney.domains.transactions.repositories.dbos.UpdateGroupDbo;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.properties.GroupProperties;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.utils.AuthorizedUser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DefaultGroupServiceTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final Currency CURRENCY_USD = Currency.getInstance("USD");
  private static final int MAX_MEMBERS = 10;
  private static final int MAX_GROUPS = 5;
  private static final int PAGE_SIZE = 2;

  private final GroupId groupId = GroupId.generate();
  private final UserId userId = UserId.generate();
  private final AuthorizedUser requester = new AuthorizedUser(UserId.generate(), Role.USER);
  private final GroupName groupName = GroupName.fromString(FakeGenerator.groupName());
  private final Description description = Description.of(FakeGenerator.description());
  private final GroupEntity group = new GroupEntity(groupId, groupName, description, CURRENCY_USD);
  private final GroupInvitationToken token = GroupInvitationToken.generate();
  private final UserEntity user =
      new UserEntity(
          userId,
          UserName.fromString(FakeGenerator.username()),
          Email.fromString(FakeGenerator.email()),
          FakeGenerator.password(),
          NOW,
          NOW,
          true,
          true);

  @Mock private UserRepository userRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private GroupInvitationRepository groupInvitationRepository;
  @Mock private GroupChecker groupChecker;
  @Spy private final Clock clock = Clock.fixed(NOW, DEFAULT_TIMEZONE_OFFSET);
  @Mock private GroupProperties groupProperties;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private DefaultGroupService groupService;

  @Nested
  class CreateEmptyGroup {

    @Test
    void returns_id_when_repository_succeeds() {
      final GroupId result =
          groupService.createEmptyGroup(groupName, description, CURRENCY_USD, requester);

      assertThat(result).isNotNull();
      verify(groupRepository)
          .create(new CreateGroupDbo(result, groupName, description, CURRENCY_USD, requester.id()));
      verify(groupRepository, never()).addUserToGroup(any(), any());
    }

    @Test
    void throws_when_create_throws() {
      final UserNotFoundException exception = new UserNotFoundException("Creator not found");
      doThrow(exception).when(groupRepository).create(any(CreateGroupDbo.class));

      assertThatThrownBy(
              () -> groupService.createEmptyGroup(groupName, description, CURRENCY_USD, requester))
          .isSameAs(exception);
      verify(groupRepository, never()).addUserToGroup(any(), any());
    }
  }

  @Nested
  class CreateGroupAndAddCreatorAsMember {

    @Test
    void returns_id_when_repository_succeeds() {
      final GroupId result =
          groupService.createGroupAndAddCreatorAsMember(
              groupName, description, CURRENCY_USD, requester);

      assertThat(result).isNotNull();
      final InOrder order = inOrder(groupRepository);
      order
          .verify(groupRepository)
          .create(new CreateGroupDbo(result, groupName, description, CURRENCY_USD, requester.id()));
      order.verify(groupRepository).addUserToGroup(requester.id(), result);
    }

    @Test
    void throws_when_create_throws() {
      final UserNotFoundException exception = new UserNotFoundException("Creator not found");
      doThrow(exception).when(groupRepository).create(any(CreateGroupDbo.class));

      assertThatThrownBy(
              () ->
                  groupService.createGroupAndAddCreatorAsMember(
                      groupName, description, CURRENCY_USD, requester))
          .isSameAs(exception);
      verify(groupRepository, never()).addUserToGroup(any(), any());
    }

    @Test
    void throws_when_addUserToGroup_throws() {
      final UserAlreadyInGroupException exception = new UserAlreadyInGroupException();
      doThrow(exception)
          .when(groupRepository)
          .addUserToGroup(eq(requester.id()), any(GroupId.class));

      assertThatThrownBy(
              () ->
                  groupService.createGroupAndAddCreatorAsMember(
                      groupName, description, CURRENCY_USD, requester))
          .isSameAs(exception);
      final ArgumentCaptor<CreateGroupDbo> captor = ArgumentCaptor.forClass(CreateGroupDbo.class);
      verify(groupRepository).create(captor.capture());
      verify(groupRepository).addUserToGroup(requester.id(), captor.getValue().id());
    }
  }

  @Nested
  class LeaveGroup {

    @Test
    void succeeds() {
      assertThatCode(() -> groupService.leaveGroup(groupId, requester)).doesNotThrowAnyException();

      final InOrder order = inOrder(groupChecker, groupRepository);
      order.verify(groupChecker).verifyGroupExists(groupId);
      order.verify(groupChecker).verifyUserIsInGroup(requester.id(), groupId);
      order.verify(groupRepository).removeUserFromGroup(requester.id(), groupId);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.leaveGroup(groupId, requester)).isSameAs(exception);
      verify(groupChecker, never()).verifyUserIsInGroup(any(), any());
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final UserIsNotMemberOfGroupException exception = new UserIsNotMemberOfGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsInGroup(requester.id(), groupId);

      assertThatThrownBy(() -> groupService.leaveGroup(groupId, requester)).isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }
  }

  @Nested
  class DeleteGroupAsAdmin {

    @Test
    void succeeds() {
      assertThatCode(() -> groupService.deleteGroupAsAdmin(groupId)).doesNotThrowAnyException();

      final InOrder order = inOrder(groupChecker, groupRepository);
      order.verify(groupChecker).verifyGroupExists(groupId);
      order.verify(groupRepository).softDeleteById(groupId);
    }

    @Test
    void throws_when_when_group_does_not_exist() {
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.deleteGroupAsAdmin(groupId)).isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }
  }

  @Nested
  class GetGroupsForUser {

    @Test
    void get_first_page_from_repository() {
      final GroupEntity secondGroup =
          new GroupEntity(
              GroupId.generate(),
              GroupName.fromString(FakeGenerator.groupName()),
              Description.of(FakeGenerator.description()),
              CURRENCY_USD);
      final PageRequest pageable = PageRequest.of(0, PAGE_SIZE);
      when(groupRepository.findByUserId(userId, pageable))
          .thenReturn(new PageImpl<>(List.of(group, secondGroup), pageable, 3));

      final Page<GroupData> result = groupService.getGroupsForUser(userId, 0, PAGE_SIZE);

      assertThat(result.getContent())
          .containsExactly(
              new GroupData(groupId, groupName, description, CURRENCY_USD),
              new GroupData(
                  secondGroup.id(), secondGroup.name(), secondGroup.description(), CURRENCY_USD));
      assertThat(result.getNumber()).isZero();
      assertThat(result.getSize()).isEqualTo(PAGE_SIZE);
      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getTotalPages()).isEqualTo(2);
      assertThat(result.hasNext()).isTrue();
      verify(groupChecker).verifyUserExists(userId);
      verify(groupRepository).findByUserId(userId, pageable);
    }

    @Test
    void returns_empty_page() {
      final PageRequest pageable = PageRequest.of(0, PAGE_SIZE);
      when(groupRepository.findByUserId(userId, pageable)).thenReturn(Page.empty(pageable));

      final Page<GroupData> result = groupService.getGroupsForUser(userId, 0, PAGE_SIZE);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
      assertThat(result.getNumber()).isZero();
      assertThat(result.getSize()).isEqualTo(PAGE_SIZE);
      verify(groupChecker).verifyUserExists(userId);
    }

    @Test
    void get_second_page_from_repository() {
      final PageRequest pageable = PageRequest.of(1, PAGE_SIZE);
      when(groupRepository.findByUserId(userId, pageable))
          .thenReturn(new PageImpl<>(List.of(group), pageable, 3));

      final Page<GroupData> result = groupService.getGroupsForUser(userId, 1, PAGE_SIZE);

      assertThat(result.getContent())
          .containsExactly(new GroupData(groupId, groupName, description, CURRENCY_USD));
      assertThat(result.getNumber()).isEqualTo(1);
      assertThat(result.getSize()).isEqualTo(PAGE_SIZE);
      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getTotalPages()).isEqualTo(2);
      assertThat(result.hasNext()).isFalse();
      verify(groupChecker).verifyUserExists(userId);
      verify(groupRepository).findByUserId(userId, pageable);
    }

    @Test
    void throws_when_user_does_not_exist() {
      final UserNotFoundException exception = new UserNotFoundException("User not found");
      doThrow(exception).when(groupChecker).verifyUserExists(userId);

      assertThatThrownBy(() -> groupService.getGroupsForUser(userId, 0, PAGE_SIZE))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }
  }

  @Nested
  class GetGroupInfo {

    @ParameterizedTest
    @EnumSource
    void returns_data(final Role role) {
      final AuthorizedUser authorizedUser = new AuthorizedUser(requester.id(), role);
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

      assertThat(groupService.getGroupInfo(groupId, authorizedUser))
          .isEqualTo(new GroupData(groupId, groupName, description, CURRENCY_USD));
      verify(groupChecker).verifyGroupExists(groupId);
      verify(groupChecker, times(role == Role.USER ? 1 : 0))
          .verifyUserIsInGroup(requester.id(), groupId);
    }

    @Test
    void throws_when_requester_is_not_member_of_the_group() {
      final UserIsNotMemberOfGroupException exception = new UserIsNotMemberOfGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsInGroup(requester.id(), groupId);

      assertThatThrownBy(() -> groupService.getGroupInfo(groupId, requester)).isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.getGroupInfo(groupId, requester)).isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }
  }

  @Nested
  class UpdateGroupInfo {

    @ParameterizedTest
    @EnumSource
    void succeeds(final Role role) {
      final AuthorizedUser authorizedUser = new AuthorizedUser(requester.id(), role);
      final UpdateGroupData data = new UpdateGroupData(groupName, description);

      assertThatCode(() -> groupService.updateGroupInfo(groupId, authorizedUser, data))
          .doesNotThrowAnyException();
      verify(groupChecker).verifyGroupExists(groupId);
      verify(groupChecker, times(role == Role.USER ? 1 : 0))
          .verifyUserIsInGroup(requester.id(), groupId);
      verify(groupRepository).updateById(groupId, new UpdateGroupDbo(groupName, description));
    }

    @Test
    void throws_when_requester_is_not_member_of_the_group() {
      final UpdateGroupData data = new UpdateGroupData(groupName, description);
      final UserIsNotMemberOfGroupException exception = new UserIsNotMemberOfGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsInGroup(requester.id(), groupId);

      assertThatThrownBy(() -> groupService.updateGroupInfo(groupId, requester, data))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final UpdateGroupData data = new UpdateGroupData(groupName, description);
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.updateGroupInfo(groupId, requester, data))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_dbo_is_empty() {
      final UpdateGroupData data = new UpdateGroupData(null, null);

      assertThatThrownBy(() -> groupService.updateGroupInfo(groupId, requester, data))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("At least one field must be provided for update");
      verifyNoInteractions(groupRepository);
    }
  }

  @Nested
  class SendInvitationToken {

    @BeforeEach()
    void beforeEach() {
      lenient().when(groupProperties.invitationDuration()).thenReturn(Duration.ofHours(1));
      lenient().when(groupProperties.maxNumInvitations()).thenReturn(5);
      lenient().when(groupProperties.maxNumUsersPerGroup()).thenReturn(MAX_MEMBERS);
      lenient().when(groupProperties.maxNumGroupsPerUser()).thenReturn(MAX_GROUPS);
      lenient()
          .when(groupProperties.waitTimeBetweenInvitations())
          .thenReturn(Duration.ofMinutes(5));
    }

    @ParameterizedTest
    @EnumSource
    void succeeds(final Role role) {
      final AuthorizedUser authorizedUser = new AuthorizedUser(requester.id(), role);
      final GroupInvitationEntity previousInvitation =
          new GroupInvitationEntity(
              groupId,
              userId,
              requester.id(),
              ExpirationTime.of(NOW),
              NOW.minus(Duration.ofMinutes(5)));
      when(groupInvitationRepository.findByKey(userId, groupId, requester.id()))
          .thenReturn(Optional.of(previousInvitation));
      when(groupRepository.countMembershipsOfGroup(groupId)).thenReturn(MAX_MEMBERS - 1);
      when(groupRepository.countMembershipsOfUser(userId)).thenReturn(MAX_GROUPS - 1);
      when(groupInvitationRepository.countByCreatedBy(requester.id())).thenReturn(4);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

      assertThatCode(() -> groupService.sendInvitationToken(groupId, userId, authorizedUser))
          .doesNotThrowAnyException();

      verify(groupChecker).verifyGroupExists(groupId);
      verify(groupChecker).verifyUserExists(userId);
      verify(groupChecker, times(role == Role.USER ? 1 : 0))
          .verifyUserIsInGroup(requester.id(), groupId);
      verify(groupChecker).verifyUserIsNotInGroup(userId, groupId);
      final ArgumentCaptor<CreateGroupInvitationDbo> captor =
          ArgumentCaptor.forClass(CreateGroupInvitationDbo.class);
      final InOrder order = inOrder(groupInvitationRepository, eventPublisher);
      order.verify(groupInvitationRepository).deleteByKey(userId, groupId, requester.id());
      order.verify(groupInvitationRepository).create(captor.capture());
      final CreateGroupInvitationDbo invitation = captor.getValue();
      assertThat(invitation.groupId()).isEqualTo(groupId);
      assertThat(invitation.userId()).isEqualTo(userId);
      assertThat(invitation.createdBy()).isEqualTo(requester.id());
      assertThat(invitation.token()).isNotNull();
      assertThat(invitation.expiresAt())
          .isEqualTo(ExpirationTime.of(NOW.plus(Duration.ofHours(1))));
      assertThat(invitation.lastSentAt()).isEqualTo(NOW);
      order
          .verify(eventPublisher)
          .publishEvent(
              new GroupInvitationRequestedEvent(
                  userId, user.email(), user.username(), groupName, invitation.token()));
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isSameAs(exception);
      verifyNoInteractions(
          groupRepository, groupInvitationRepository, userRepository, eventPublisher);
    }

    @Test
    void throws_when_destination_user_does_not_exist() {
      final UserNotFoundException exception = new UserNotFoundException("User not found");
      doThrow(exception).when(groupChecker).verifyUserExists(userId);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isSameAs(exception);
      verifyNoInteractions(
          groupRepository, groupInvitationRepository, userRepository, eventPublisher);
    }

    @Test
    void throws_when_requester_is_not_member_of_the_group() {
      final UserIsNotMemberOfGroupException exception = new UserIsNotMemberOfGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsInGroup(requester.id(), groupId);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isSameAs(exception);
      verifyNoInteractions(
          groupRepository, groupInvitationRepository, userRepository, eventPublisher);
    }

    @Test
    void throws_when_user_is_already_in_the_group() {
      final UserAlreadyInGroupException exception = new UserAlreadyInGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsNotInGroup(userId, groupId);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isSameAs(exception);
      verifyNoInteractions(
          groupRepository, groupInvitationRepository, userRepository, eventPublisher);
    }

    @Test
    void throws_when_group_is_full() {
      when(groupRepository.countMembershipsOfGroup(groupId)).thenReturn(MAX_MEMBERS);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isInstanceOf(MaximumGroupMembersReachedException.class);
      verifyNoInteractions(groupInvitationRepository, userRepository, eventPublisher);
    }

    @Test
    void throws_when_user_can_not_join_more_groups() {
      when(groupRepository.countMembershipsOfUser(userId)).thenReturn(MAX_GROUPS);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isInstanceOf(MaximumGroupsPerUserReachedException.class);
      verifyNoInteractions(groupInvitationRepository, userRepository, eventPublisher);
    }

    @Test
    void throws_when_maximum_number_of_invitations_reached() {
      when(groupInvitationRepository.countByCreatedBy(requester.id())).thenReturn(5);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isInstanceOf(MaximumGroupInvitationsReachedException.class);
      verify(groupInvitationRepository, never()).create(any());
      verifyNoInteractions(userRepository, eventPublisher);
    }

    @Test
    void throws_when_user_is_already_invited() {
      when(groupInvitationRepository.existsByGroupIdAndUserId(groupId, userId)).thenReturn(true);

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isInstanceOf(UserAlreadyInvitedException.class);
      verify(groupInvitationRepository, never()).create(any());
      verifyNoInteractions(userRepository, eventPublisher);
    }

    @Test
    void throws_when_request_is_too_soon() {
      final GroupInvitationEntity invitation =
          new GroupInvitationEntity(
              groupId, userId, requester.id(), ExpirationTime.of(NOW.plusSeconds(1)), NOW);
      when(groupInvitationRepository.findByKey(userId, groupId, requester.id()))
          .thenReturn(Optional.of(invitation));

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, userId, requester))
          .isInstanceOf(SendGroupInvitationTooSoonException.class);
      verify(groupInvitationRepository, never()).deleteByKey(any(), any(), any());
      verify(groupInvitationRepository, never()).create(any());
      verifyNoInteractions(userRepository, eventPublisher);
    }
  }

  @Nested
  class EnterToGroupWithToken {

    @Test
    void succeeds() {
      final GroupInvitationEntity invitation =
          new GroupInvitationEntity(
              groupId, requester.id(), userId, ExpirationTime.of(NOW.plusSeconds(1)), NOW);
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));
      when(groupProperties.maxNumUsersPerGroup()).thenReturn(MAX_MEMBERS);
      when(groupProperties.maxNumGroupsPerUser()).thenReturn(MAX_GROUPS);
      when(groupRepository.countMembershipsOfGroup(groupId)).thenReturn(MAX_MEMBERS - 1);
      when(groupRepository.countMembershipsOfUser(requester.id())).thenReturn(MAX_GROUPS - 1);

      assertThatCode(() -> groupService.enterToGroupWithToken(token, requester))
          .doesNotThrowAnyException();

      final InOrder order = inOrder(groupChecker, groupInvitationRepository, groupRepository);
      order.verify(groupChecker).verifyGroupExists(groupId);
      order.verify(groupInvitationRepository).deleteByToken(token);
      order.verify(groupRepository).addUserToGroup(requester.id(), groupId);
    }

    @Test
    void throws_when_token_is_missing() {
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, requester))
          .isInstanceOf(GroupInvitationInvalidException.class)
          .hasMessage("Invitation token not found");
      verify(groupInvitationRepository, never()).deleteByToken(any());
      verifyNoInteractions(groupChecker, groupRepository);
    }

    @Test
    void throws_when_token_is_expired() {
      final GroupInvitationEntity invitation =
          new GroupInvitationEntity(
              groupId, requester.id(), userId, ExpirationTime.of(NOW.minusSeconds(1)), NOW);
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, requester))
          .isInstanceOf(GroupInvitationInvalidException.class)
          .hasMessage("Invitation token expired");
      verify(groupInvitationRepository, never()).deleteByToken(any());
      verifyNoInteractions(groupChecker, groupRepository);
    }

    @Test
    void throws_when_token_receiver_and_requester_are_not_the_same() {
      final GroupInvitationEntity invitation =
          new GroupInvitationEntity(
              groupId, userId, requester.id(), ExpirationTime.of(NOW.plusSeconds(1)), NOW);
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, requester))
          .isInstanceOf(WrongUserForInvitationException.class);
      verify(groupInvitationRepository, never()).deleteByToken(any());
      verifyNoInteractions(groupChecker, groupRepository);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupInvitationEntity invitation =
          new GroupInvitationEntity(
              groupId, requester.id(), userId, ExpirationTime.of(NOW.plusSeconds(1)), NOW);
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, requester))
          .isSameAs(exception);
      verify(groupInvitationRepository, never()).deleteByToken(any());
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_group_is_full() {
      final GroupInvitationEntity invitation =
          new GroupInvitationEntity(
              groupId, requester.id(), userId, ExpirationTime.of(NOW.plusSeconds(1)), NOW);
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));
      when(groupProperties.maxNumUsersPerGroup()).thenReturn(MAX_MEMBERS);
      when(groupRepository.countMembershipsOfGroup(groupId)).thenReturn(MAX_MEMBERS);

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, requester))
          .isInstanceOf(MaximumGroupMembersReachedException.class);
      verify(groupInvitationRepository, never()).deleteByToken(any());
      verify(groupRepository, never()).addUserToGroup(any(), any());
    }

    @Test
    void throws_when_user_can_not_join_more_groups() {
      final GroupInvitationEntity invitation =
          new GroupInvitationEntity(
              groupId, requester.id(), userId, ExpirationTime.of(NOW.plusSeconds(1)), NOW);
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));
      when(groupProperties.maxNumUsersPerGroup()).thenReturn(MAX_MEMBERS);
      when(groupProperties.maxNumGroupsPerUser()).thenReturn(MAX_GROUPS);
      when(groupRepository.countMembershipsOfUser(requester.id())).thenReturn(MAX_GROUPS);

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, requester))
          .isInstanceOf(MaximumGroupsPerUserReachedException.class);
      verify(groupInvitationRepository, never()).deleteByToken(any());
      verify(groupRepository, never()).addUserToGroup(any(), any());
    }
  }

  @Nested
  class GetUsersInGroup {

    @ParameterizedTest
    @EnumSource
    void returns_list(final Role role) {
      final AuthorizedUser authorizedUser = new AuthorizedUser(requester.id(), role);
      final UserEntity secondUser =
          new UserEntity(
              UserId.generate(),
              UserName.fromString(FakeGenerator.username()),
              Email.fromString(FakeGenerator.email()),
              FakeGenerator.password(),
              NOW,
              NOW,
              true,
              true);
      when(groupRepository.findUsersByGroupId(groupId)).thenReturn(List.of(user, secondUser));

      assertThat(groupService.getUsersInGroup(groupId, authorizedUser))
          .containsExactly(
              new MemberData(userId, user.username()),
              new MemberData(secondUser.id(), secondUser.username()));
      verify(groupChecker).verifyGroupExists(groupId);
      verify(groupChecker, times(role == Role.USER ? 1 : 0))
          .verifyUserIsInGroup(requester.id(), groupId);
    }

    @Test
    void returns_empty_list_when_group_has_no_members() {
      when(groupRepository.findUsersByGroupId(groupId)).thenReturn(List.of());

      assertThat(groupService.getUsersInGroup(groupId, requester)).isEmpty();
      verify(groupChecker).verifyGroupExists(groupId);
      verify(groupChecker).verifyUserIsInGroup(requester.id(), groupId);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final UserIsNotMemberOfGroupException exception = new UserIsNotMemberOfGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsInGroup(requester.id(), groupId);

      assertThatThrownBy(() -> groupService.getUsersInGroup(groupId, requester))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.getUsersInGroup(groupId, requester))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }
  }

  @Nested
  class AddUserToGroupAsAdmin {

    @Test
    void succeeds() {
      when(groupProperties.maxNumUsersPerGroup()).thenReturn(MAX_MEMBERS);
      when(groupProperties.maxNumGroupsPerUser()).thenReturn(MAX_GROUPS);
      when(groupRepository.countMembershipsOfGroup(groupId)).thenReturn(MAX_MEMBERS - 1);
      when(groupRepository.countMembershipsOfUser(userId)).thenReturn(MAX_GROUPS - 1);

      assertThatCode(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .doesNotThrowAnyException();

      final InOrder order = inOrder(groupChecker, groupRepository);
      order.verify(groupChecker).verifyGroupExists(groupId);
      order.verify(groupChecker).verifyUserExists(userId);
      order.verify(groupChecker).verifyUserIsNotInGroup(userId, groupId);
      order.verify(groupRepository).addUserToGroup(userId, groupId);
    }

    @Test
    void throws_when_group_does_not_exist() {
      doThrow(new GroupNotFoundException("Group not found"))
          .when(groupChecker)
          .verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void throws_when_user_to_add_does_not_exist() {
      doThrow(new UserNotFoundException("User not found"))
          .when(groupChecker)
          .verifyUserExists(userId);

      assertThatThrownBy(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void throws_when_user_is_already_in_group() {
      final UserAlreadyInGroupException exception = new UserAlreadyInGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsNotInGroup(userId, groupId);

      assertThatThrownBy(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_group_is_full() {
      when(groupProperties.maxNumUsersPerGroup()).thenReturn(MAX_MEMBERS);
      when(groupRepository.countMembershipsOfGroup(groupId)).thenReturn(MAX_MEMBERS);

      assertThatThrownBy(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .isInstanceOf(MaximumGroupMembersReachedException.class);
      verify(groupRepository, never()).addUserToGroup(any(), any());
    }

    @Test
    void throws_when_user_can_not_join_more_groups() {
      when(groupProperties.maxNumUsersPerGroup()).thenReturn(MAX_MEMBERS);
      when(groupProperties.maxNumGroupsPerUser()).thenReturn(MAX_GROUPS);
      when(groupRepository.countMembershipsOfUser(userId)).thenReturn(MAX_GROUPS);

      assertThatThrownBy(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .isInstanceOf(MaximumGroupsPerUserReachedException.class);
      verify(groupRepository, never()).addUserToGroup(any(), any());
    }
  }

  @Nested
  class RemoveUserFromGroup {

    @ParameterizedTest
    @EnumSource
    void succeeds(final Role role) {
      final AuthorizedUser authorizedUser = new AuthorizedUser(requester.id(), role);

      assertThatCode(() -> groupService.removeUserFromGroup(groupId, userId, authorizedUser))
          .doesNotThrowAnyException();
      verify(groupChecker).verifyGroupExists(groupId);
      verify(groupChecker).verifyUserExists(userId);
      verify(groupChecker, times(role == Role.USER ? 1 : 0))
          .verifyUserIsInGroup(requester.id(), groupId);
      verify(groupRepository).removeUserFromGroup(userId, groupId);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final UserIsNotMemberOfGroupException exception = new UserIsNotMemberOfGroupException();
      doThrow(exception).when(groupChecker).verifyUserIsInGroup(requester.id(), groupId);

      assertThatThrownBy(() -> groupService.removeUserFromGroup(groupId, userId, requester))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupNotFoundException exception = new GroupNotFoundException("Group not found");
      doThrow(exception).when(groupChecker).verifyGroupExists(groupId);

      assertThatThrownBy(() -> groupService.removeUserFromGroup(groupId, userId, requester))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }

    @Test
    void throws_when_user_to_remove_does_not_exist() {
      final UserNotFoundException exception = new UserNotFoundException("User not found");
      doThrow(exception).when(groupChecker).verifyUserExists(userId);

      assertThatThrownBy(() -> groupService.removeUserFromGroup(groupId, userId, requester))
          .isSameAs(exception);
      verifyNoInteractions(groupRepository);
    }
  }

  @Nested
  class GetGroupBalances {
    // TODO
  }

  @Nested
  class GetGroupTransactions {
    // TODO
  }

  @Nested
  class ForceSyncBalancesAsAdmin {
    // TODO
  }
}
