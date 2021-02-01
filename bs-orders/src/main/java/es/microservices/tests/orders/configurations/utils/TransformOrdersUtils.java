package es.microservices.tests.orders.configurations.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.Assert;
import es.microservices.tests.orders.dtos.CustomerDto;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.PhoneOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasCustomerDto;
import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasPhoneOrderDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransformOrdersUtils {

  public static OrderDto transformOrderDto(final DaasOrderDto input) {
    Assert.notNull(input, "'input' must be not null");
    // @formatter:off
    return DaasOrderDto2OrderDtoMapper.map(input);
    // @formatter:on
  }

  public static DaasNewOrderDto transformNewOrderDto(final NewOrderDto newOrder,
      final List<DaasPhoneDto> phonesToBuy) {
    Assert.notNull(newOrder, "'newOrder' must be not null");
    Assert.notNull(phonesToBuy, "'phonesToBuy' must be not null");
    Assert.isTrue((!phonesToBuy.isEmpty()), "'phonesToBuy' must be not empty");
    // @formatter:off
    return DaasNewOrderDto.builder()
        .customerData(map(newOrder.getCustomerData()))
        .phoneListToBuy(map(phonesToBuy))
        .totalPrice(calculateTotalPrice(phonesToBuy))
        .build();
    // @formatter:on
  }

  private static DaasCustomerDto map(CustomerDto input) {
    // @formatter:off
    return DaasCustomerDto.builder()
        .name(input.getName())
        .surname(input.getSurname())
        .email(input.getEmail())
        .build();
    // @formatter:on
  }

  private static Collection<? extends DaasPhoneOrderDto> map(final List<DaasPhoneDto> input) {
    // @formatter:off
    return input.stream()
        .map(TransformOrdersUtils::map)
        .collect(Collectors.toList());
    // @formatter:on
  }

  private static DaasPhoneOrderDto map(DaasPhoneDto input) {
    // @formatter:off
    return DaasPhoneOrderDto.builder()
        .phoneId(input.getPhoneId())
        .name(input.getName())
        .price(input.getPrice())
        .build();
    // @formatter:on
  }

  private static Double calculateTotalPrice(List<DaasPhoneDto> phonesToBuy) {
    // @formatter:off
    return phonesToBuy.stream()
        .map(DaasPhoneDto::getPrice)
        .collect(Collectors.reducing(Double::sum))
        .orElse(0.0);
    // @formatter:on
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class DaasOrderDto2OrderDtoMapper {

    public static OrderDto map(final DaasOrderDto input) {
      // @formatter:off
      return OrderDto.builder()
          .orderId(input.getOrderId())
          .customerData(map(input.getCustomerData()))
          .phoneListToBuy(map(input.getPhoneListToBuy()))
          .totalPrice(input.getTotalPrice())
          .build();
      // @formatter:on
    }

    private static CustomerDto map(DaasCustomerDto input) {
      // @formatter:off
      return CustomerDto.builder()
          .name(input.getName())
          .surname(input.getSurname())
          .email(input.getEmail())
          .build();
      // @formatter:on
    }

    private static Collection<? extends PhoneOrderDto> map(final List<DaasPhoneOrderDto> input) {
      // @formatter:off
      return input.stream()
          .map(DaasOrderDto2OrderDtoMapper::map)
          .collect(Collectors.toList());
      // @formatter:on
    }

    private static PhoneOrderDto map(final DaasPhoneOrderDto input) {
      // @formatter:off
      return PhoneOrderDto.builder()
          .phoneId(input.getPhoneId())
          .name(input.getName())
          .price(input.getPrice())
          .build();
      // @formatter:on
    }


  }
}
