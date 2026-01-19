package com.familymoney.familymoney.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.repositories.IRoleRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.services.UserService;
import com.familymoney.familymoney.services.mappers.UserDataMapper;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class UserServiceTests {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private IUserRepository userRepository;
  @Mock private IRoleRepository roleRepository;
  @Spy private UserPasswordEncoder passwordEncoder;
  @Spy private UserDataMapper userDataMapper = Mappers.getMapper(UserDataMapper.class);

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
                UserDbo.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .hashedPassword("hashedpassword")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(false)
                    .isEnabled(true)
                    .build()));

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
