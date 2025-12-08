package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.services.data.UpdateUserData;
import com.familymoney.familymoney.types.*;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

  @NonNull GetUserData getUserData(@NonNull UserId userId);

  void deleteUser(@NonNull UserId userId);

  void updateUserInfo(@NonNull UserId userId, @NonNull UpdateUserData data);

  @NonNull Page<@NonNull GetUserData> getUsers(Pageable pageable);

  void enableUser(@NonNull UserId userId, boolean enabled);

  void setUserRole(@NonNull UserId userId, @NonNull Role role);

  @NonNull Role getUserRole(@NonNull UserId userId);
}
