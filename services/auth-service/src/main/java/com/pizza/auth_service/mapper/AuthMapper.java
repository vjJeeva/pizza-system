package com.pizza.auth_service.mapper;

import com.pizza.auth_service.dto.RegisterRequest;
import com.pizza.auth_service.entity.AuthUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthUser toEntity(RegisterRequest request);
}
