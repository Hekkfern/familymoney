package com.familymoney.domains.transactions.controllers.dtos;

import java.util.Currency;
import java.util.UUID;

public record GroupDto(UUID id, String name, String description, Currency currency) {}
