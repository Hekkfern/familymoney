package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.BalanceDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateBalanceDbo;
import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import java.util.List;
import java.util.Optional;
import org.javamoney.moneta.Money;

public interface IBalanceRepository {

  Optional<BalanceDbo> create(GroupId groupId, Money amount, UserId user1, UserId user2);

  List<BalanceDbo> findByUserAndGroup(UserId userId, GroupId groupId);

  boolean updateById(final BalanceId id, UpdateBalanceDbo data);

  Optional<BalanceDbo> findById(BalanceId id);
}
