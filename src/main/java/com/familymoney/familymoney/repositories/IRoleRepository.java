package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.UserId;

public interface IRoleRepository {

  Role getRoleByUserId(UserId userId);

  void setRoleForUserId(UserId userId, Role role);
}
