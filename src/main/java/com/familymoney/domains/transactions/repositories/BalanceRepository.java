package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.BalanceKey;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.GroupId;
import java.util.List;
import java.util.Optional;
import org.javamoney.moneta.Money;

/** Repository interface for persisting balances between two members of a group. */
public interface BalanceRepository {

  void create(BalanceKey key);

  void incrementByKey(BalanceKey key, Money delta);

  Optional<BalanceEntity> findByKey(BalanceKey key);

  List<BalanceEntity> findByGroupId(GroupId groupId);
}
