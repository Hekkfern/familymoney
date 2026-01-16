package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dbos.UpdateTransactionDbo;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UpdateTransactionDataMapper {

  UpdateTransactionDbo toDbo(UpdateTransactionData data);
}
