package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import java.util.Currency;

public record GroupEntity(GroupId id, GroupName name, Description description, Currency currency) {}
