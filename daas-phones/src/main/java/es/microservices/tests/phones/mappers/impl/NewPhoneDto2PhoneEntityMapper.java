package es.microservices.tests.phones.mappers.impl;

import java.util.UUID;
import org.springframework.util.Assert;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import es.microservices.tests.phones.mappers.Mapper;

public class NewPhoneDto2PhoneEntityMapper implements Mapper<NewPhoneDto, PhoneEntity> {

  @Override
  public PhoneEntity map(NewPhoneDto input) {
    Assert.notNull(input, "'input' must not be null");
    // @formatter:off
    return PhoneEntity.builder()
        .id(UUID.randomUUID().toString())
        .name(input.getName())
        .description(input.getDescription())
        .price(input.getPrice())
        .imageURL(input.getImageURL())
        .build();    
    // @formatter:on 
  }

}
