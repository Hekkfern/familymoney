package com.familymoney.familymoney.services;

import com.familymoney.familymoney.repositories.IRoleRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.UpdateUserDbo;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.services.data.UpdateUserData;
import com.familymoney.familymoney.services.mappers.GetUserDataMapper;
import com.familymoney.familymoney.types.*;
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
  private final GetUserDataMapper getUserDataMapper;

  @Override
  public Optional<GetUserData> getUserData(UserId userId) {
    val userOpt = userRepository.findById(userId);
    return userOpt.map(getUserDataMapper::fromDbo);
  }

  @Override
  public void deleteUser(UserId userId) {
    userRepository.deleteById(userId);
  }

  @Transactional
  @Override
  public void updateUserInfo(UserId userId, UpdateUserData data) {
    if (!data.isEmpty()) {
      userRepository.updateById(
          userId,
          UpdateUserDbo.builder()
              .username(data.getUsername())
              .email(data.getEmail())
              .hashedPassword(
                  data.getPassword() != null
                      ? passwordEncoder.encode(data.getPassword().value())
                      : null)
              .build());
    }
  }

  @Override
  public Page<GetUserData> getUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(getUserDataMapper::fromDbo);
  }

  @Override
  public void enableUser(UserId userId, boolean enabled) {
    userRepository.updateById(userId, UpdateUserDbo.builder().isEnabled(enabled).build());
  }

  @Override
  public void setUserRole(UserId userId, Role role) {
    roleRepository.setRoleForUserId(userId, role);
  }

  @Override
  public Optional<Role> getUserRole(UserId userId) {
    return roleRepository.getRoleByUserId(userId);
  }

  @Override
  public void createAdminUser(Username username, Email email, Password password) {
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
    userRepository.updateById(userDb.id(), UpdateUserDbo.builder().isEmailVerified(true).build());
    log.info("Admin user created successfully");
  }
}
