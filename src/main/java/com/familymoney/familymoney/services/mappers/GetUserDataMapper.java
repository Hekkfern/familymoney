package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.services.data.GetUserData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GetUserDataMapper {

  GetUserData fromDbo(UserDbo userDbo);
}
