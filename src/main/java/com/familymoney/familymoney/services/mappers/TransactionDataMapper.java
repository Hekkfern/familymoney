package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dbos.TransactionDbo;
import com.familymoney.familymoney.services.data.TransactionData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionDataMapper {

  TransactionData fromDbo(TransactionDbo transactionDbo);
}
