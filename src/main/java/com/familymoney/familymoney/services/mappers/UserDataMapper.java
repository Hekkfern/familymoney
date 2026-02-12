package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.entities.UserEntity;
import com.familymoney.familymoney.services.data.UserData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDataMapper {

  UserData fromDbo(UserEntity userEntity);
}
