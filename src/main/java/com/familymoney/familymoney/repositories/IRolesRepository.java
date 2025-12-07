package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.UserId;
import org.jspecify.annotations.NonNull;

public interface IRolesRepository {

  @NonNull String getRoleByUserId(@NonNull UserId userId);

  void setRoleForUserId(@NonNull UserId userId, @NonNull String role);
}
