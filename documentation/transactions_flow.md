# Money transactions flow explained

All the steps of the money transactions flow use endpoints defined in either the [
`IGroupController`](/src/main/java/com/familymoney/familymoney/controllers/IGroupController.java) or the [
`ITransactionController`](/src/main/java/com/familymoney/familymoney/controllers/ITransactionController.java) classes.

The first step in the money transactions flow is to create a money group by sending a request to `/api/v1/groups` with
the name of the group. The user who creates the group will be automatically added to it as a member.

After the group is created, the user can add other members to the group by sending a request to
`/api/v1/groups/{groupId}/invitation`, which will generate an invitation code that can be shared with another user. The
invited user can then use this code to join the group by sending a request to `/api/v1/groups/invitation`. Each
invitation code can only be used once, and it will expire after 24 hours.

Once the group is set up and has members, any member can create a transaction by sending a request to
`/api/v1/groups/{groupId}/transactions` with the transaction details, including the amount, description, and the IDs of
the members involved in the transaction.

When a transaction is created, the system will automatically calculate the amount each member owes or is owed based on
the transaction details. The transaction will be added to the group's transaction history, and the balances of the
involved members will be updated accordingly. The balances can be viewed by sending a request to
`/api/v1/groups/{groupId}/balances`, which will return the current balances of all members in the group.
