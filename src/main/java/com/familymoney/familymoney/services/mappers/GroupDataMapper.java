package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dbos.GroupDbo;
import com.familymoney.familymoney.services.data.GroupData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GroupDataMapper {

  GroupData fromDbo(GroupDbo userDbo);
}
