package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.UserId;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface IPermissionsRepository {

    @NonNull
    List<String> getPermissionsByUserId(@NonNull UserId userId);

    void setPermissionsForUserId(@NonNull UserId userId, @NonNull List<String> permissions);

    void deletePermissionsByUserId(@NonNull UserId userId, @NonNull List<String> permissions);

    void setRoleForUserId(@NonNull UserId userId, @NonNull String role);
}
