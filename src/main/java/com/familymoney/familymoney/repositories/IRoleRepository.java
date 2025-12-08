package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.UserId;
import org.jspecify.annotations.NonNull;

public interface IRoleRepository {

  @NonNull Role getRoleByUserId(@NonNull UserId userId);

  void setRoleForUserId(@NonNull UserId userId, @NonNull Role role);
}
