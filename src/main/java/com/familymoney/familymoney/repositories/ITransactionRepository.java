package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.TransactionDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateTransactionDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.util.Optional;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITransactionRepository {

  Optional<TransactionDbo> create(
      String description, GroupId groupId, Money amount, UserId lender, UserId borrower);

  boolean updateById(TransactionId id, UpdateTransactionDbo data);

  boolean deleteById(TransactionId id);

  Optional<TransactionDbo> findById(TransactionId id);

  Page<TransactionDbo> findAllByGroupId(GroupId groupId, Pageable pageable);
}
