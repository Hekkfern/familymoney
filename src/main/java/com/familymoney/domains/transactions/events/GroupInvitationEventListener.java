package com.familymoney.domains.transactions.events;

/** Listener contract for group invitation emails. */
public interface GroupInvitationEventListener {

  /**
   * Sends the invitation email after the transaction commits.
   *
   * @param event group invitation email request
   */
  void onGroupInvitationRequested(GroupInvitationRequestedEvent event);
}
