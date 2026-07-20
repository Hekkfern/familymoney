package com.familymoney.domains.user.services;

import com.familymoney.domains.user.repositories.IRoleRepository;
import com.familymoney.domains.user.repositories.IUserRepository;
import com.familymoney.domains.user.repositories.dtos.CreateUserDto;
import com.familymoney.domains.user.repositories.dtos.UpdateUserDto;
import com.familymoney.domains.user.services.data.UpdateUserData;
import com.familymoney.domains.user.services.data.UserData;
import com.familymoney.domains.user.services.mappers.UserDataMapper;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.security.UserPasswordEncoder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

  private final IUserRepository userRepository;
  private final IRoleRepository roleRepository;
  private final UserPasswordEncoder passwordEncoder;

  @Override
  public Optional<UserData> getUserData(final UserId userId) {
    val userOpt = userRepository.findById(userId);
    return userOpt.map(UserDataMapper::fromDbo);
  }

  @Override
  public void deleteUser(final UserId userId) {
    userRepository.deleteById(userId);
  }

  @Transactional
  @Override
  public void updateUserInfo(final UserId userId, UpdateUserData data) {
    if (!data.isEmpty()) {
      userRepository.updateById(
          userId,
          UpdateUserDto.builder()
              .username(data.username())
              .email(data.email())
              .hashedPassword(
                  data.password() != null ? passwordEncoder.encode(data.password().value()) : null)
              .build());
    }
  }

  @Override
  public Page<UserData> getUsers(final Pageable pageable) {
    return userRepository.getAll(pageable).map(UserDataMapper::fromDbo);
  }

  @Override
  public void enableUser(final UserId userId, final boolean enabled) {
    userRepository.updateById(userId, UpdateUserDto.builder().isEnabled(enabled).build());
  }

  @Override
  public void setUserRole(final UserId userId, final Role role) {
    roleRepository.setRoleForUserId(userId, role);
  }

  @Override
  public Optional<Role> getUserRole(final UserId userId) {
    return roleRepository.getRoleByUserId(userId);
  }

  @Override
  public void createAdminUser(final UserName username, final Email email, final Password password) {
    // Check if user already exists
    if (userRepository.existsByEmailOrUsername(email, username)) {
      log.info("Admin user already exists, skipping creation");
      return;
    }
    // Create user
    val userId = UserId.generate();
    val userDbOpt =
        userRepository.create(
            CreateUserDto.builder()
                .id(userId)
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password.value()))
                .build());
    if (userDbOpt.isEmpty()) {
      log.error("Could not create user in the database");
      return;
    }
    // Assign admin permissions
    roleRepository.setRoleForUserId(userId, Role.ADMIN);
    // verify email
    userRepository.updateById(userId, UpdateUserDto.builder().isEmailVerified(true).build());
    log.info("Admin user created successfully");
  }
}
