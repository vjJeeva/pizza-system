package com.pizza.user_service.mapper;

import com.pizza.user_service.dto.UserRequest;
import com.pizza.user_service.entity.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring") // This tells Spring to treat it as a @Component
public interface UserMapper {

    User toEntity(UserRequest userRequest);

    UserRequest toDto(User user);
}