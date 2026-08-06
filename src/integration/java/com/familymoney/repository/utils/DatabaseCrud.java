package com.familymoney.repository.utils;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.Balances;
import com.familymoney.generated.tables.EmailVerificationTokens;
import com.familymoney.generated.tables.GroupInvitations;
import com.familymoney.generated.tables.Groups;
import com.familymoney.generated.tables.PasswordResetTokens;
import com.familymoney.generated.tables.RefreshTokens;
import com.familymoney.generated.tables.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;

@RequiredArgsConstructor
public class DatabaseCrud {

  private final DSLContext dslContext;

  public void insertUser(
      final UserId userId,
      final UserName username,
      final Email email,
      final String hashedPassword,
      final Instant createdAt,
      final boolean isEmailVerified,
      final boolean isEnabled) {
    final OffsetDateTime createdAtDateTime = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    dslContext
        .insertInto(Users.USERS)
        .columns(
            Users.USERS.ID,
            Users.USERS.USERNAME,
            Users.USERS.EMAIL,
            Users.USERS.HASHED_PASSWORD,
            Users.USERS.CREATED_AT,
            Users.USERS.UPDATED_AT,
            Users.USERS.IS_EMAIL_VERIFIED,
            Users.USERS.IS_ENABLED)
        .values(
            userId.value(),
            username.value(),
            email.value(),
            hashedPassword,
            createdAtDateTime,
            createdAtDateTime,
            isEmailVerified,
            isEnabled)
        .execute();
  }

  public void insertRefreshToken(
      final UUID id,
      final UserId userId,
      final RefreshToken token,
      final Instant createdAt,
      final com.familymoney.domains.auth.types.ExpirationTime expiresAt,
      final TokenFamily family) {
    final OffsetDateTime createdAtDateTime = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    final OffsetDateTime expiresAtDateTime =
        OffsetDateTime.ofInstant(expiresAt.value(), ZoneOffset.UTC);
    dslContext
        .insertInto(RefreshTokens.REFRESH_TOKENS)
        .columns(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.UPDATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .values(
            id,
            userId.value(),
            token.value(),
            createdAtDateTime,
            createdAtDateTime,
            expiresAtDateTime,
            family.value())
        .execute();
  }

  public void insertEmailVerificationToken(
      final UserId userId,
      final EmailVerificationToken token,
      final Instant createdAt,
      final com.familymoney.domains.auth.types.ExpirationTime expiresAt) {
    final OffsetDateTime createdAtDateTime = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    final OffsetDateTime expiresAtDateTime =
        OffsetDateTime.ofInstant(expiresAt.value(), ZoneOffset.UTC);
    dslContext
        .insertInto(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .columns(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT)
        .values(
            userId.value(),
            token.value(),
            expiresAtDateTime,
            createdAtDateTime,
            createdAtDateTime,
            createdAtDateTime)
        .execute();
  }

  public void insertPasswordResetToken(
      final UserId userId,
      final PasswordResetToken token,
      final Instant createdAt,
      final com.familymoney.domains.auth.types.ExpirationTime expiresAt) {
    final OffsetDateTime createdAtDateTime = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    final OffsetDateTime expiresAtDateTime =
        OffsetDateTime.ofInstant(expiresAt.value(), ZoneOffset.UTC);
    dslContext
        .insertInto(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .columns(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.UPDATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT)
        .values(
            userId.value(), token.value(), createdAtDateTime, createdAtDateTime, expiresAtDateTime)
        .execute();
  }

  public void insertGroup(
      final GroupId id,
      final GroupName name,
      final String description,
      final CurrencyUnit currency,
      final Instant createdAt) {
    final OffsetDateTime createdAtDateTime = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    dslContext
        .insertInto(Groups.GROUPS)
        .columns(
            Groups.GROUPS.ID,
            Groups.GROUPS.NAME,
            Groups.GROUPS.DESCRIPTION,
            Groups.GROUPS.CURRENCY_CODE,
            Groups.GROUPS.CREATED_AT,
            Groups.GROUPS.UPDATED_AT)
        .values(
            id.value(),
            name.value(),
            description,
            currency.getCurrencyCode(),
            createdAtDateTime,
            createdAtDateTime)
        .execute();
  }

  public void insertGroupInvitation(
      final UUID id,
      final GroupId groupId,
      final UserId userId,
      final GroupInvitationToken token,
      final Instant createdAt,
      final com.familymoney.domains.transactions.types.ExpirationTime expiresAt) {
    final OffsetDateTime createdAtDateTime = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    final OffsetDateTime expiresAtDateTime =
        OffsetDateTime.ofInstant(expiresAt.value(), ZoneOffset.UTC);
    dslContext
        .insertInto(GroupInvitations.GROUP_INVITATIONS)
        .columns(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.USER_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN,
            GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .values(
            id,
            groupId.value(),
            userId.value(),
            token.value(),
            createdAtDateTime,
            expiresAtDateTime)
        .execute();
  }

  public void insertBalance(
      final BalanceId id,
      final GroupId groupId,
      final Money amount,
      final UserId user1,
      final UserId user2) {
    dslContext
        .insertInto(Balances.BALANCES)
        .columns(
            Balances.BALANCES.ID,
            Balances.BALANCES.GROUP_ID,
            Balances.BALANCES.AMOUNT,
            Balances.BALANCES.CURRENCY_CODE,
            Balances.BALANCES.USER_ID_1,
            Balances.BALANCES.USER_ID_2)
        .values(
            id.value(),
            groupId.value(),
            amount.getNumber().numberValue(BigDecimal.class),
            amount.getCurrency().getCurrencyCode(),
            user1.value(),
            user2.value())
        .execute();
  }
}
