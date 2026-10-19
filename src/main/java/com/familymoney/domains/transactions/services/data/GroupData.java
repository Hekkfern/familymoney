package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import java.util.Currency;

public record GroupData(GroupId id, GroupName name, Description description, Currency currency) {}
