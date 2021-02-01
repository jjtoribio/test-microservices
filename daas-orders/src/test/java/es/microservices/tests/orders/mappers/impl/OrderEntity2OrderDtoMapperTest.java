package es.microservices.tests.orders.mappers.impl;

import static es.microservices.tests.orders.features.TestFeatures.createCollection;
import static es.microservices.tests.orders.features.TestFeatures.createOrderEntity;
import static es.microservices.tests.orders.features.TestFeatures.createPhoneData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.entities.OrderEntity.PhoneData;
import es.microservices.tests.orders.mappers.Mapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderEntity2OrderDtoMapperTest {

  private static Mapper<OrderEntity, OrderDto> mapper;

  @BeforeAll
  public static void beforeAll() {
    mapper = new OrderEntity2OrderDtoMapper();
  }

  @Test
  void orderEntity_input_isNull() {
    final OrderEntity input = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(input);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void orderEntity_input_mapOk() {
    final String orderId = "orderId";
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final Collection<PhoneData> phoneListToBuy =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final OrderEntity input = createOrderEntity(orderId, customerName, customerSurname,
        customerEmail, phoneListToBuy, totalPrice);

    final OrderDto orderDto = mapper.map(input);

    evaluateResult(input, orderDto);
  }



  @Test
  void flux_orderEntity_input_isNull() {
    final OrderEntity input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Flux.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void flux_orderEntity_flux_isNull() {
    Flux<OrderEntity> flux = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(flux);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void flux_orderEntity_input_mapOk() {
    final String orderId = "orderId";
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final Collection<PhoneData> phoneListToBuy =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final OrderEntity input = createOrderEntity(orderId, customerName, customerSurname,
        customerEmail, phoneListToBuy, totalPrice);

    final Flux<OrderDto> entityFlux = mapper.map(Flux.just(input));

    // @formatter:off
    StepVerifier.create(entityFlux)
      .assertNext(orderDto -> evaluateResult(input, orderDto))
      .expectComplete()
      .verify();
    // @formatter:on
  }


  @Test
  void mono_orderEntity_input_isNull() {
    final OrderEntity input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Mono.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void mono_orderEntity_mono_isNull() {
    Mono<OrderEntity> mono = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(mono);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void mono_orderEntity_input_mapOk() {
    final String orderId = "orderId";
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final Collection<PhoneData> phoneListToBuy =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final OrderEntity input = createOrderEntity(orderId, customerName, customerSurname,
        customerEmail, phoneListToBuy, totalPrice);

    final Mono<OrderDto> entityMono = mapper.map(Mono.just(input));
    // @formatter:off
    StepVerifier.create(entityMono)
      .assertNext(orderDto -> evaluateResult(input, orderDto))
      .expectComplete()
      .verify();
    // @formatter:on
  }

  private static void evaluateResult(final OrderEntity input, final OrderDto orderDto) {
    assertNotNull(orderDto);
    assertTrue(StringUtils.isNotBlank(orderDto.getOrderId()));
    assertNotNull(input.getCustomerData());
    assertNotNull(input.getPhoneListToBuy());
    assertNotNull(input.getTotalPrice());

    assertEquals(input.getId(), orderDto.getOrderId());

    assertEquals(input.getCustomerData().getName(), orderDto.getCustomerData().getName());
    assertEquals(input.getCustomerData().getSurname(), orderDto.getCustomerData().getSurname());
    assertEquals(input.getCustomerData().getEmail(), orderDto.getCustomerData().getEmail());

    assertEquals(input.getPhoneListToBuy().size(), orderDto.getPhoneListToBuy().size());

    assertEquals(input.getPhoneListToBuy().get(0).getPhoneId(),
        orderDto.getPhoneListToBuy().get(0).getPhoneId());
    assertEquals(input.getPhoneListToBuy().get(0).getName(),
        orderDto.getPhoneListToBuy().get(0).getName());
    assertEquals(input.getPhoneListToBuy().get(0).getPrice(),
        orderDto.getPhoneListToBuy().get(0).getPrice());

    assertEquals(input.getPhoneListToBuy().get(1).getPhoneId(),
        orderDto.getPhoneListToBuy().get(1).getPhoneId());
    assertEquals(input.getPhoneListToBuy().get(1).getName(),
        orderDto.getPhoneListToBuy().get(1).getName());
    assertEquals(input.getPhoneListToBuy().get(1).getPrice(),
        orderDto.getPhoneListToBuy().get(1).getPrice());

    assertEquals(input.getTotalPrice().doubleValue(), orderDto.getTotalPrice().doubleValue());
  }


}
