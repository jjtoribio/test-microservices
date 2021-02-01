package es.microservices.tests.orders.mappers.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.util.Assert;
import es.microservices.tests.orders.dtos.CustomerDto;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.PhoneDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.entities.OrderEntity.CustomerData;
import es.microservices.tests.orders.entities.OrderEntity.PhoneData;
import es.microservices.tests.orders.mappers.Mapper;

public class NewOrderDto2OrderEntityMapper implements Mapper<NewOrderDto, OrderEntity> {

  @Override
  public OrderEntity map(NewOrderDto input) {
    Assert.notNull(input, "'input' must not be null");
    // @formatter:off
    return OrderEntity.builder()
        .id(UUID.randomUUID().toString())
        .customerData(map(input.getCustomerData()))
        .phoneListToBuy(map(input.getPhoneListToBuy()))
        .totalPrice(input.getTotalPrice())
        .build();    
    // @formatter:on 
  }

  private static Collection<PhoneData> map (Collection<PhoneDto> phones) {
    // @formatter:off
    return phones.stream()
        .map(NewOrderDto2OrderEntityMapper::map)
        .collect(Collectors.toList());
    // @formatter:on 
  }
  
  private static PhoneData map (PhoneDto input) {
    // @formatter:off
    return PhoneData.builder()
        .name(input.getName())
        .phoneId(input.getPhoneId())
        .price(input.getPrice())
        .build();
    // @formatter:on 
  }
  
  private static CustomerData map(final CustomerDto input) {
    // @formatter:off
    return CustomerData.builder()
        .name(input.getName())
        .surname(input.getSurname())
        .email(input.getEmail())
        .build();
    // @formatter:on
  }
}
