package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dbos.UpdateGroupDbo;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UpdateGroupDataMapper {

  UpdateGroupDbo toDbo(UpdateGroupData data);
}
