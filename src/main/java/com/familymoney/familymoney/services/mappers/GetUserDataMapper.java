package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.types.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GetUserDataMapper {

  GetUserData fromDbo(UserDbo userDbo);
}
