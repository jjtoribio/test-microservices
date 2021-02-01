package es.microservices.tests.orders.mappers.impl;

import static es.microservices.tests.orders.features.TestFeatures.createCollection;
import static es.microservices.tests.orders.features.TestFeatures.createNewOrderDto;
import static es.microservices.tests.orders.features.TestFeatures.createPhoneDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.PhoneDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.mappers.Mapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class NewOrderDto2OrderEntityMapperTest {

  private static Mapper<NewOrderDto, OrderEntity> mapper;

  @BeforeAll
  public static void beforeAll() {
    mapper = new NewOrderDto2OrderEntityMapper();
  }

  @Test
  void newOrderDto_input_isNull() {
    final NewOrderDto input = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(input);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void newOrderDto_input_mapOk() {
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

    final Collection<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));

    final NewOrderDto input =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    final OrderEntity entity = mapper.map(input);

    evaluateResult(input, entity);
  }



  @Test
  void flux_newOrderDto_input_isNull() {
    final NewOrderDto input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Flux.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void flux_newOrderDto_flux_isNull() {
    Flux<NewOrderDto> flux = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(flux);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void flux_newOrderDto_input_mapOk() {
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

    final Collection<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));

    final NewOrderDto input =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    final Flux<OrderEntity> entityFlux = mapper.map(Flux.just(input));

    // @formatter:off
    StepVerifier.create(entityFlux)
      .assertNext(entity -> evaluateResult(input, entity))
      .expectComplete()
      .verify();
    // @formatter:on

  }


  @Test
  void mono_newOrderDto_input_isNull() {
    final NewOrderDto input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Mono.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void mono_newOrderDto_mono_isNull() {
    Mono<NewOrderDto> mono = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(mono);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void mono_newOrderDto_input_mapOk() {
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

    final Collection<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));

    final NewOrderDto input =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    final Mono<OrderEntity> entityMono = mapper.map(Mono.just(input));

    // @formatter:off
    StepVerifier.create(entityMono)
      .assertNext(entity -> evaluateResult(input, entity))
      .expectComplete()
      .verify();
    // @formatter:on
  }

  private static void evaluateResult(final NewOrderDto input, final OrderEntity entity) {
    assertNotNull(entity);
    assertTrue(StringUtils.isNotBlank(entity.getId()));
    assertNotNull(input.getCustomerData());
    assertNotNull(input.getPhoneListToBuy());
    assertNotNull(input.getTotalPrice());

    assertEquals(input.getCustomerData().getName(), entity.getCustomerData().getName());
    assertEquals(input.getCustomerData().getSurname(), entity.getCustomerData().getSurname());
    assertEquals(input.getCustomerData().getEmail(), entity.getCustomerData().getEmail());

    assertEquals(input.getPhoneListToBuy().size(), entity.getPhoneListToBuy().size());

    assertEquals(input.getPhoneListToBuy().get(0).getPhoneId(),
        entity.getPhoneListToBuy().get(0).getPhoneId());
    assertEquals(input.getPhoneListToBuy().get(0).getName(),
        entity.getPhoneListToBuy().get(0).getName());
    assertEquals(input.getPhoneListToBuy().get(0).getPrice(),
        entity.getPhoneListToBuy().get(0).getPrice());

    assertEquals(input.getPhoneListToBuy().get(1).getPhoneId(),
        entity.getPhoneListToBuy().get(1).getPhoneId());
    assertEquals(input.getPhoneListToBuy().get(1).getName(),
        entity.getPhoneListToBuy().get(1).getName());
    assertEquals(input.getPhoneListToBuy().get(1).getPrice(),
        entity.getPhoneListToBuy().get(1).getPrice());

    assertEquals(input.getTotalPrice().doubleValue(), entity.getTotalPrice().doubleValue());
  }

}
