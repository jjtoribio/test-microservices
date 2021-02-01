package es.microservices.tests.phones.features;


import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class TestFeatures {

  public static PhoneEntity createPhoneEntity(final String id, final String name,
      final String description, final Double price, final String imageURL) {
    return PhoneEntity.builder().id(id).name(name).description(description).price(price)
        .imageURL(imageURL).build();
  }
  
  public static NewPhoneDto createNewPhoneDto(final String name,
      final String description, final Double price, final String imageURL) {
    return NewPhoneDto.builder().name(name).description(description).price(price)
        .imageURL(imageURL).build();
  }
  
  public static PhoneDto createPhoneDto(final String phoneId,final String name,
      final String description, final Double price, final String imageURL) {
    return PhoneDto.builder().phoneId(phoneId).name(name).description(description).price(price)
        .imageURL(imageURL).build();
  }
  

}