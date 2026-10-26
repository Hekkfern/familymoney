package com.familymoney.domains.transactions.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GroupInvitationRequestedEventTest {

  @Nested
  class Constructor {

    @Test
    void preserves_invitation_details() {
      final UserId userId = UserId.generate();
      final Email email = Email.fromString("invitee@example.com");
      final UserName username = UserName.fromString("Invitee");
      final GroupName groupName = GroupName.fromString("Family");
      final GroupInvitationToken token = GroupInvitationToken.generate();

      final GroupInvitationRequestedEvent event =
          new GroupInvitationRequestedEvent(userId, email, username, groupName, token);

      assertThat(event.userId()).isEqualTo(userId);
      assertThat(event.email()).isEqualTo(email);
      assertThat(event.username()).isEqualTo(username);
      assertThat(event.groupName()).isEqualTo(groupName);
      assertThat(event.invitationToken()).isEqualTo(token);
    }
  }
}
