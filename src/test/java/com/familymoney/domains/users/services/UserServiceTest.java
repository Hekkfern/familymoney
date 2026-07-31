package com.familymoney.domains.users.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.familymoney.domains.users.repositories.IRoleRepository;
import com.familymoney.domains.users.repositories.IUserRepository;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.security.UserPasswordEncoder;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private IUserRepository userRepository;
  @Mock private IRoleRepository roleRepository;
  @Spy private UserPasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  // region getUserData() tests

  @Test
  void getUserdata_gets_user_data_successfully() {
    val userId = UserId.fromUuid(UUID.randomUUID());
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());

    when(userRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new UserEntity(userId, username, email, "hashedpassword", now, now, false, true)));

    val dataOpt = userService.getUserData(userId);
    assertTrue(dataOpt.isPresent());
    val data = dataOpt.get();
    assertEquals(username, data.username());
    assertEquals(email, data.email());
    assertEquals(now, data.createdAt());
    assertTrue(data.isEnabled());
    assertFalse(data.isEmailVerified());
  }

  @Test
  void getUserdata_user_not_found_returns_empty_optional() {
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    val dataOpt = userService.getUserData(userId);
    assertTrue(dataOpt.isEmpty());
  }

  // endregion
}
