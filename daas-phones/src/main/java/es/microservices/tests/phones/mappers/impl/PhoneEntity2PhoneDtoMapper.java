package es.microservices.tests.phones.mappers.impl;

import org.springframework.util.Assert;
import es.microservices.tests.phones.mappers.Mapper;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;

public class PhoneEntity2PhoneDtoMapper implements Mapper<PhoneEntity, PhoneDto> {

  @Override
  public PhoneDto map(final PhoneEntity input) {
    Assert.notNull(input, "'input' must not be null");
    // @formatter:off
    return PhoneDto.builder()
        .phoneId(input.getId())
        .name(input.getName())
        .description(input.getDescription())
        .price(input.getPrice())
        .imageURL(input.getImageURL())
        .build();    
    // @formatter:on 
  }

}
