package es.microservices.tests.orders.configurations.utils;

import java.util.Arrays;
import es.microservices.tests.orders.dtos.CustomerDto;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.PhoneOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasCustomerDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasPhoneOrderDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneCatalogDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneDto;

public class TestFeatures {
  
  public static DaasPhoneCatalogDto createDaasPhoneCatalogDto(final Integer page, final Integer pageSize, final Long totalCount, final DaasPhoneDto ... phones) {
    // @formatter:off
    return DaasPhoneCatalogDto.builder()
        .phones(Arrays.asList(phones))
        .page(page)
        .pageSize(pageSize)
        .totalCount(totalCount)
        .build();
    // @formatter:on
  }

  public static OrderDto createOrderDto(final String orderId, final String customerName,
      final String customerSurname, final String customerEmail, final Double totalPrice,
      final PhoneOrderDto ...phones) {
    // @formatter:off
    return OrderDto.builder()        
        .orderId(orderId)
        .customerData(createCustomerDto(customerName, customerSurname, customerEmail))
        .phoneListToBuy(Arrays.asList(phones))
        .totalPrice(totalPrice)
        .build(); 
    // @formatter:on
  }
  
  public static DaasOrderDto createDaasOrderDto(final String orderId, final String customerName,
      final String customerSurname, final String customerEmail, final Double totalPrice,
      final DaasPhoneOrderDto... phones) {
    // @formatter:off
    return DaasOrderDto.builder()        
        .orderId(orderId)
        .customerData(createDaasCustomerDto(customerName, customerSurname, customerEmail))
        .phoneListToBuy(Arrays.asList(phones))
        .totalPrice(totalPrice)
        .build(); 
    // @formatter:on
  }

  public static PhoneOrderDto createPhoneOrderDto(final String phoneId, final String name,
      final Double price) {
    // @formatter:off
    return PhoneOrderDto.builder()        
        .phoneId(phoneId)        
        .name(name)
        .price(price)
        .build(); 
    // @formatter:on
  }
  
  public static DaasPhoneOrderDto createDaasPhoneOrderDto(final String phoneId, final String name,
      final Double price) {
    // @formatter:off
    return DaasPhoneOrderDto.builder()        
        .phoneId(phoneId)        
        .name(name)
        .price(price)
        .build(); 
    // @formatter:on
  }

  public static DaasCustomerDto createDaasCustomerDto(final String customerName,
      final String customerSurname, final String customerEmail) {
    // @formatter:off
    return DaasCustomerDto.builder()        
        .name(customerName)
        .surname(customerSurname)
        .email(customerEmail)
        .build(); 
    // @formatter:on
  }

  public static DaasPhoneDto createDaasPhoneDto(final String phoneId, final String name,
      final String description, final String imageURL, final Double price) {
    // @formatter:off
    return DaasPhoneDto.builder()
        .phoneId(phoneId)
        .name(name)
        .description(description)
        .imageURL(imageURL)
        .price(price)
        .build(); 
    // @formatter:on
  }

  public static NewOrderDto createNewOrderDto(final String customerName,
      final String customerSurname, final String customerEmail, final String ... phoneIds) {
    // @formatter:off
    return NewOrderDto.builder()
        .customerData(createCustomerDto(customerName, customerSurname, customerEmail))
        .phoneIdListToBuy(Arrays.asList(phoneIds))        
        .build();
    // @formatter:on
  }

  public static CustomerDto createCustomerDto(final String customerName,
      final String customerSurname, final String customerEmail) {
    // @formatter:off
    return CustomerDto.builder()        
        .name(customerName)
        .surname(customerSurname)
        .email(customerEmail)
        .build(); 
    // @formatter:on
  }
  
}
