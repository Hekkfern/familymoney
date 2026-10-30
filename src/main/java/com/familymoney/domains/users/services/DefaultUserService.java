package com.familymoney.domains.users.services;

import com.familymoney.domains.users.repositories.RoleRepository;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.repositories.dbos.CreateUserDbo;
import com.familymoney.domains.users.repositories.dbos.UpdateUserDbo;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.services.data.UpdateUserData;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.services.mappers.UserDataMapper;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.security.UserPasswordEncoder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultUserService implements UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserPasswordEncoder passwordEncoder;

  @Override
  public Optional<UserData> getUserData(final UserId userId) {
    final Optional<UserEntity> userOpt = userRepository.findById(userId);
    return userOpt.map(UserDataMapper::fromDbo);
  }

  @Override
  public void deleteUser(final UserId userId) {
    userRepository.deleteById(userId);
  }

  @Transactional
  @Override
  public void updateUserInfo(final UserId userId, final UpdateUserData data) {
    userRepository.updateById(
        userId,
        UpdateUserDbo.builder()
            .username(data.username())
            .email(data.email())
            .hashedPassword(
                data.password() != null ? passwordEncoder.encode(data.password().value()) : null)
            .build());
  }

  @Override
  public Page<UserData> getUsers(final Pageable pageable) {
    return userRepository.getAll(pageable).map(UserDataMapper::fromDbo);
  }

  @Override
  public void enableUser(final UserId userId, final boolean enabled) {
    userRepository.updateById(userId, UpdateUserDbo.builder().isEnabled(enabled).build());
  }

  @Override
  public void setUserRole(final UserId userId, final Role role) {
    roleRepository.setRoleForUserId(userId, role);
  }

  @Override
  public Optional<Role> getUserRole(final UserId userId) {
    return roleRepository.getRoleByUserId(userId);
  }

  @Transactional
  @Override
  public void createAdminUser(final UserName username, final Email email, final Password password) {
    if (userRepository.existsByEmailOrUsername(email, username)) {
      log.info("Admin user already exists, skipping creation");
      return;
    }
    final UserId userId = UserId.generate();
    userRepository.create(
        new CreateUserDbo(
            userId, username, email, passwordEncoder.encode(password.value()), true, true));
    roleRepository.setRoleForUserId(userId, Role.ADMIN);
    userRepository.updateById(userId, UpdateUserDbo.builder().isEmailVerified(true).build());
    log.info("Admin user created successfully");
  }
}
