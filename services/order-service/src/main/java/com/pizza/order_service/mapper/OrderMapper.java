package com.pizza.order_service.mapper;

import com.pizza.order_service.dto.OrderRequest;
import com.pizza.order_service.dto.OrderResponse;
import com.pizza.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Ignore internal fields that the user shouldn't provide in a request
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "status", ignore = true)
    Order toEntity(OrderRequest request);

    OrderResponse toResponse(Order order);
}