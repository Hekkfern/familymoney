package com.familymoney.domains.transactions.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.events.GroupInvitationRequestedEvent;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dbos.CreateGroupInvitationDbo;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.properties.GroupProperties;
import com.familymoney.utils.AuthorizedUser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DefaultGroupServiceInvitationTest {

  private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

  @Mock private UserRepository userRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private GroupInvitationRepository groupInvitationRepository;
  @Mock private GroupChecker groupChecker;
  @Mock private GroupProperties groupProperties;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Spy private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  @InjectMocks private DefaultGroupService groupService;

  @Nested
  class SendInvitationToken {

    private final GroupId groupId = GroupId.generate();
    private final UserId invitedUserId = UserId.generate();
    private final AuthorizedUser requester = new AuthorizedUser(UserId.generate(), Role.USER);
    private final UserEntity invitedUser =
        new UserEntity(
            invitedUserId,
            UserName.fromString("Invitee"),
            Email.fromString("invitee@example.com"),
            "hashed-password",
            NOW,
            NOW,
            true,
            true);
    private final GroupEntity group =
        new GroupEntity(
            groupId,
            GroupName.fromString("Family"),
            Description.of("Family expenses"),
            Currency.getInstance("USD"));

    @BeforeEach
    void setup() {
      when(groupProperties.maxNumUsersPerGroup()).thenReturn(10);
      when(groupProperties.maxNumGroupsPerUser()).thenReturn(100);
      when(groupProperties.maxNumInvitations()).thenReturn(5);
      when(groupProperties.invitationDuration()).thenReturn(Duration.ofHours(1));
      when(userRepository.findById(invitedUserId)).thenReturn(Optional.of(invitedUser));
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    }

    @Test
    void persists_the_invited_user_and_publishes_matching_email_details() {
      final ArgumentCaptor<CreateGroupInvitationDbo> invitation =
          ArgumentCaptor.forClass(CreateGroupInvitationDbo.class);
      final ArgumentCaptor<GroupInvitationRequestedEvent> event =
          ArgumentCaptor.forClass(GroupInvitationRequestedEvent.class);

      groupService.sendInvitationToken(groupId, invitedUserId, requester);

      final InOrder order = inOrder(groupInvitationRepository, eventPublisher);
      order.verify(groupInvitationRepository).create(invitation.capture());
      order.verify(eventPublisher).publishEvent(event.capture());
      assertThat(invitation.getValue().userId()).isEqualTo(invitedUserId);
      assertThat(invitation.getValue().createdBy()).isEqualTo(requester.id());
      assertThat(invitation.getValue().groupId()).isEqualTo(groupId);
      assertThat(event.getValue())
          .isEqualTo(
              new GroupInvitationRequestedEvent(
                  invitedUserId,
                  invitedUser.email(),
                  invitedUser.username(),
                  group.name(),
                  invitation.getValue().token()));
    }

    @Test
    void does_not_publish_when_persistence_fails() {
      final DatabaseExecutionException failure =
          new DatabaseExecutionException("Unable to create invitation");
      doThrow(failure).when(groupInvitationRepository).create(any(CreateGroupInvitationDbo.class));

      assertThatThrownBy(() -> groupService.sendInvitationToken(groupId, invitedUserId, requester))
          .isSameAs(failure);

      verifyNoInteractions(eventPublisher);
    }
  }
}
