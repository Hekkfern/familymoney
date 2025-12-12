package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserRepository {

  Optional<UserDbo> create(Username username, Email email, String passwordHash);

  Optional<UserDbo> findById(UserId id);

  Optional<UserDbo> findByEmail(Email email);

  Optional<UserDbo> findByUsername(Username username);

  boolean existsByEmailOrUsername(Email email, Username username);

  void updateInfo(UserId id, Optional<Username> username, Optional<Email> email);

  void updatePassword(UserId id, String newPasswordHash);

  void deleteById(UserId id);

  void deleteByIsUnverifiedAndOlderThan(Duration cutoff);

  void setIsEnabledByUserId(UserId id, boolean isEnabled);

  void verifyEmail(UserId userId);

  Page<UserDbo> findAll(Pageable pageable);
}
