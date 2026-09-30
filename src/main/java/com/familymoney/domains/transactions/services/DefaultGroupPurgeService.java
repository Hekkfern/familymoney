package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.repositories.BalanceRepository;
import com.familymoney.domains.transactions.repositories.ExpenseRepository;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.PaymentRepository;
import com.familymoney.properties.GroupDeletionProperties;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Default implementation of {@link GroupPurgeService}. */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupPurgeService implements GroupPurgeService {

  private final GroupRepository groupRepository;
  private final ExpenseRepository expenseRepository;
  private final PaymentRepository paymentRepository;
  private final BalanceRepository balanceRepository;
  private final GroupInvitationRepository groupInvitationRepository;
  private final GroupDeletionProperties groupDeletionProperties;
  private final Clock clock;

  @Override
  public int purgeDeletedGroupsBatch() {
    final Instant deletedBefore = Instant.now(clock).minus(groupDeletionProperties.retention());
    final int batchSize = groupDeletionProperties.deleteBatchSize();

    final int deletedExpenses =
        expenseRepository.deleteExpensesOfPurgeableGroups(deletedBefore, batchSize);
    final int deletedPayments =
        paymentRepository.deletePaymentsOfPurgeableGroups(deletedBefore, batchSize);
    final int deletedBalances =
        balanceRepository.deleteBalancesOfPurgeableGroups(deletedBefore, batchSize);
    final int deletedInvitations =
        groupInvitationRepository.deleteInvitationsOfPurgeableGroups(deletedBefore, batchSize);
    final int deletedMemberships =
        groupRepository.deleteMembershipsOfPurgeableGroups(deletedBefore, batchSize);
    final int deletedGroups = groupRepository.deletePurgeableGroups(deletedBefore, batchSize);

    log.debug(
        "Purge pass deleted {} expenses, {} payments, {} balances, {} invitations, {} memberships"
            + " and {} groups",
        deletedExpenses,
        deletedPayments,
        deletedBalances,
        deletedInvitations,
        deletedMemberships,
        deletedGroups);
    return deletedExpenses
        + deletedPayments
        + deletedBalances
        + deletedInvitations
        + deletedMemberships
        + deletedGroups;
  }
}
