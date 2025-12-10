package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Duration;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserRepository {

  @NonNull Optional<UserDbo> create(
      @NonNull Username username, @NonNull Email email, @NonNull String passwordHash);

  @NonNull Optional<UserDbo> findById(@NonNull UserId id);

  @NonNull Optional<UserDbo> findByEmail(@NonNull Email email);

  @NonNull Optional<UserDbo> findByUsername(@NonNull Username username);

  boolean existsByEmailOrUsername(@NonNull Email email, @NonNull Username username);

  void updateInfo(
      @NonNull UserId id, @NonNull Optional<Username> username, @NonNull Optional<Email> email);

  void updatePassword(@NonNull UserId id, @NonNull String newPasswordHash);

  void deleteById(@NonNull UserId id);

  void deleteByIsUnverifiedAndOlderThan(@NonNull Duration cutoff);

  void setIsEnabledByUserId(@NonNull UserId id, boolean isEnabled);

  void verifyEmail(@NonNull UserId userId);

  @NonNull Page<@NonNull UserDbo> findAll(Pageable pageable);
}
