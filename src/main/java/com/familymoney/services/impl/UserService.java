package com.familymoney.services.impl;

import com.familymoney.repositories.IRoleRepository;
import com.familymoney.repositories.IUserRepository;
import com.familymoney.repositories.dtos.CreateUserDto;
import com.familymoney.repositories.dtos.UpdateUserDto;
import com.familymoney.security.UserPasswordEncoder;
import com.familymoney.services.IUserService;
import com.familymoney.services.data.UpdateUserData;
import com.familymoney.services.data.UserData;
import com.familymoney.services.mappers.UserDataMapper;
import com.familymoney.types.*;
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
  public Optional<UserData> getUserData(UserId userId) {
    val userOpt = userRepository.findById(userId);
    return userOpt.map(UserDataMapper::fromDbo);
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
          UpdateUserDto.builder()
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
  public Page<UserData> getUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(UserDataMapper::fromDbo);
  }

  @Override
  public void enableUser(UserId userId, boolean enabled) {
    userRepository.updateById(userId, UpdateUserDto.builder().isEnabled(enabled).build());
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
  public void createAdminUser(UserName username, Email email, Password password) {
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
