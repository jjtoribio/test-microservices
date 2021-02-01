package es.microservices.tests.orders.features;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import es.microservices.tests.orders.dtos.CustomerDto;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.PhoneDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.entities.OrderEntity.CustomerData;
import es.microservices.tests.orders.entities.OrderEntity.PhoneData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class TestFeatures {

  public static OrderEntity createOrderEntity(final String id, final String customerName,
      final String customerSurname, final String customerEmail,
      final Collection<PhoneData> phoneListToBuy, final Double totalPrice) {
    // @formatter:off
    return OrderEntity.builder()
        .id(id)
        .customerData(createCustomerData(customerName, customerSurname, customerEmail))
        .phoneListToBuy(phoneListToBuy)
        .totalPrice(totalPrice)
        .build();
    // @formatter:on
  }

  public static CustomerData createCustomerData(final String name, final String surname,
      final String email) {
    // @formatter:off
    return CustomerData.builder()
        .name(name)
        .surname(surname)
        .email(email)
        .build();
    // @formatter:on
  }
  
  public static PhoneData createPhoneData(final String phoneId, final String name, 
      final Double price) {
    // @formatter:off
    return PhoneData.builder()
        .phoneId(phoneId)
        .name(name)
        .price(price)
        .build();
    // @formatter:on
  }
  
  public static CustomerDto createCustomerDto(final String name, final String surname,
      final String email) {
    // @formatter:off
    return CustomerDto.builder()
        .name(name)
        .surname(surname)
        .email(email)
        .build();
    // @formatter:on
  }
  
  public static PhoneDto createPhoneDto(final String phoneId, final String name, 
      final Double price) {
    // @formatter:off
    return PhoneDto.builder()
        .phoneId(phoneId)
        .name(name)
        .price(price)
        .build();
    // @formatter:on
  }

  public static List<PhoneDto> createCollection(PhoneDto ... phones) {
    return Arrays.asList(phones);
  }
  
  public static List<PhoneData> createCollection(PhoneData ... phones) {
    return Arrays.asList(phones);
  }
  

  public static NewOrderDto createNewOrderDto(final String customerName,
      final String customerSurname, final String customerEmail,
      final Collection<PhoneDto> phoneListToBuy, final Double totalPrice) {
    // @formatter:off
    return NewOrderDto.builder()
        .customerData(createCustomerDto(customerName, customerSurname, customerEmail))
        .phoneListToBuy(phoneListToBuy)
        .totalPrice(totalPrice)
        .build();
    // @formatter:on
  }

  public static OrderDto createOrderDto(final String orderId, final String customerName,
      final String customerSurname, final String customerEmail,
      final Collection<PhoneDto> phoneListToBuy, final Double totalPrice) {
    // @formatter:off
    return OrderDto.builder()
        .orderId(orderId)
        .customerData(createCustomerDto(customerName, customerSurname, customerEmail))
        .phoneListToBuy(phoneListToBuy)        
        .totalPrice(totalPrice)
        .build();
    // @formatter:on
  }


}
