package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.services.data.GetUserData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GetUserDataMapper {

  GetUserData fromDbo(UserDbo userDbo);
}
