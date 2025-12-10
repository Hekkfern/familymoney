package com.familymoney.familymoney.services;

import com.familymoney.familymoney.repositories.IRoleRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.services.data.UpdateUserData;
import com.familymoney.familymoney.services.mappers.GetUserDataMapper;
import com.familymoney.familymoney.types.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
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
  private final GetUserDataMapper getUserDataMapper;

  @Override
  public @NonNull Optional<GetUserData> getUserData(@NonNull UserId userId) {
    val userOpt = userRepository.findById(userId);
    return userOpt.map(getUserDataMapper::fromDbo);
  }

  @Override
  public void deleteUser(@NonNull UserId userId) {
    userRepository.deleteById(userId);
  }

  @Transactional
  @Override
  public void updateUserInfo(@NonNull UserId userId, @NonNull UpdateUserData data) {
    // change user info
    if (data.username().isPresent() || data.password().isPresent()) {
      userRepository.updateInfo(userId, data.username(), data.email());
    }
    // change user password
    data.password()
        .ifPresent(p -> userRepository.updatePassword(userId, passwordEncoder.encode(p.value())));
  }

  @Override
  public @NonNull Page<@NonNull GetUserData> getUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(getUserDataMapper::fromDbo);
  }

  @Override
  public void enableUser(@NonNull UserId userId, boolean enabled) {
    userRepository.setIsEnabledByUserId(userId, enabled);
  }

  @Override
  public void setUserRole(@NonNull UserId userId, @NonNull Role role) {
    roleRepository.setRoleForUserId(userId, role);
  }

  @Override
  public @NonNull Role getUserRole(@NonNull UserId userId) {
    return roleRepository.getRoleByUserId(userId);
  }

  @Override
  public void createAdminUser(
          @NonNull Username username, @NonNull Email email, @NonNull Password password) {
    // Check if user already exists
    if (userRepository.existsByEmailOrUsername(email, username)) {
      log.info("Admin user already exists, skipping creation");
      return;
    }
    // Create user
    val userDbOpt =
        userRepository.create(username, email, passwordEncoder.encode(password.value()));
    if (userDbOpt.isEmpty()) {
      log.error("Could not create user in the database");
      return;
    }
    val userDb = userDbOpt.get();
    // Assign user permissions (default role)
    roleRepository.setRoleForUserId(userDb.id(), Role.ADMIN);
    // verify email
    userRepository.verifyEmail(userDb.id());
    log.info("Admin user created successfully");
  }
}
