package com.familymoney.domains.users.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.records.UsersRecord;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID userUuid = UUID.randomUUID();
      final String username = "john";
      final String email = "john@example.com";
      final String hashedPassword = "hashed-password";
      final UsersRecord r = new UsersRecord();
      r.setId(userUuid);
      r.setUsername(username);
      r.setEmail(email);
      r.setHashedPassword(hashedPassword);
      r.setIsEmailVerified(true);
      r.setIsEnabled(true);

      final UserEntity entity = UserJooqMapper.toEntity(r);

      assertThat(entity.id()).isEqualTo(UserId.fromUuid(userUuid));
      assertThat(entity.username()).isEqualTo(UserName.fromString(username));
      assertThat(entity.email()).isEqualTo(Email.fromString(email));
      assertThat(entity.hashedPassword()).isEqualTo(hashedPassword);
      assertThat(entity.isEmailVerified()).isTrue();
      assertThat(entity.isEnabled()).isTrue();
    }
  }
}
