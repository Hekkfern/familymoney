package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Duration;
import java.util.Optional;
import org.springframework.lang.NonNull;

public interface IUserRepository {

  @NonNull
  Optional<UserDbo> create(
      @NonNull Username username, @NonNull Email email, @NonNull String passwordHash);

  @NonNull
  Optional<UserDbo> findById(@NonNull UserId id);

  @NonNull
  Optional<UserDbo> findByEmail(@NonNull Email email);

  @NonNull
  Optional<UserDbo> findByUsername(@NonNull Username username);

  boolean existsByEmailOrUsername(@NonNull Email email, @NonNull Username username);

  void update(
      @NonNull UserId id, @NonNull Optional<Username> username, @NonNull Optional<Email> email);

  void updatePassword(@NonNull UserId id, @NonNull String newPasswordHash);

  void deleteById(@NonNull UserId id);

  void deleteByIsUnverifiedAndOlderThan(@NonNull Duration cutoff);
}
