package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;

/**
 * Represents payment data returned by the service.
 *
 * @param id the payment identifier
 * @param groupId the group identifier
 * @param description the payment description
 * @param amount the paid monetary amount
 * @param doneAt the payment completion time
 * @param fromUserId the user who made the payment
 * @param toUserId the user who received the payment
 */
public record PaymentData(
    PaymentId id,
    GroupId groupId,
    Description description,
    Money amount,
    Instant doneAt,
    UserId fromUserId,
    UserId toUserId) {}
