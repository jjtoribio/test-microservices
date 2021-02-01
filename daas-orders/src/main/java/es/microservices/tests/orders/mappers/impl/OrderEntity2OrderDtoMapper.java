package es.microservices.tests.orders.mappers.impl;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.util.Assert;
import es.microservices.tests.orders.mappers.Mapper;
import es.microservices.tests.orders.dtos.CustomerDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.PhoneDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.entities.OrderEntity.CustomerData;
import es.microservices.tests.orders.entities.OrderEntity.PhoneData;

public class OrderEntity2OrderDtoMapper implements Mapper<OrderEntity, OrderDto> {

  @Override
  public OrderDto map(final OrderEntity input) {
    Assert.notNull(input, "'input' must not be null");
    // @formatter:off
    return OrderDto.builder()
        .orderId(input.getId())
        .customerData(map(input.getCustomerData()))
        .phoneListToBuy(map(input.getPhoneListToBuy()))
        .totalPrice(input.getTotalPrice())
        .build();    
    // @formatter:on 
  }
  
  private static Collection<PhoneDto> map (final Collection<PhoneData> phones) {
    // @formatter:off
    return phones.stream()
          .map(OrderEntity2OrderDtoMapper::map)
          .collect(Collectors.toList());
    // @formatter:on
  }
  
  private static PhoneDto map(final PhoneData input) {
    // @formatter:off
    return PhoneDto.builder()
        .phoneId(input.getPhoneId())
        .name(input.getName())
        .price(input.getPrice())
        .build();
    // @formatter:on
  }
  
  private static CustomerDto map (final CustomerData input) {
    // @formatter:off
    return CustomerDto.builder()
        .name(input.getName())
        .surname(input.getSurname())
        .email(input.getEmail())
        .build();
    // @formatter:on
  }

}
