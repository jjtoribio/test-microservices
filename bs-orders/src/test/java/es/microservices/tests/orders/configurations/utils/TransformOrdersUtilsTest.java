package es.microservices.tests.orders.configurations.utils;

import static es.microservices.tests.orders.configurations.utils.TestFeatures.createDaasOrderDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createDaasPhoneDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createDaasPhoneOrderDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createNewOrderDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasPhoneOrderDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneDto;

class TransformOrdersUtilsTest {

  @Test
  void transformOrderDto_input_is_null_must_throws_illegalArgumentException() {
    final DaasOrderDto input = null;

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> TransformOrdersUtils.transformOrderDto(input));

    final String expectedMessage = "'input' must be not null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }


  @Test
  void transformOrderDto_runOk() {
    final String orderId = "orderId";
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final String phoneId = "phoneId";
    final String phoneName = "phoneName";
    final Double phonePrice = 10.0;

    final Double totalPrice = phonePrice;
    final DaasPhoneOrderDto phone = createDaasPhoneOrderDto(phoneId, phoneName, phonePrice);
    final DaasOrderDto input = createDaasOrderDto(orderId, customerName, customerSurname,
        customerEmail, totalPrice, phone);


    final OrderDto actualOrder = TransformOrdersUtils.transformOrderDto(input);

    assertNotNull(actualOrder);
    assertEquals(input.getOrderId(), actualOrder.getOrderId());
    assertNotNull(actualOrder.getCustomerData());
    assertEquals(input.getCustomerData().getName(), actualOrder.getCustomerData().getName());
    assertEquals(input.getCustomerData().getSurname(), actualOrder.getCustomerData().getSurname());
    assertEquals(input.getCustomerData().getEmail(), actualOrder.getCustomerData().getEmail());
    assertNotNull(actualOrder.getPhoneListToBuy());
    assertEquals(1, actualOrder.getPhoneListToBuy().size());
    assertEquals(input.getTotalPrice(), actualOrder.getTotalPrice());
  }

  @Test
  void transformNewOrderDtoo_newOrder_is_null_must_throws_illegalArgumentException() {
    final String phoneId = "phoneId";
    final String name = "name";
    final String description = "description";
    final String imageURL = "imageURL";
    final Double price = 10.0;
    final NewOrderDto newOrder = null;

    final List<DaasPhoneDto> phonesToBuy =
        Arrays.asList(createDaasPhoneDto(phoneId, name, description, imageURL, price));

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> TransformOrdersUtils.transformNewOrderDto(newOrder, phonesToBuy));

    final String expectedMessage = "'newOrder' must be not null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void transformNewOrderDto_phonesToBuy_is_null_must_throws_illegalArgumentException() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";
    final String phoneId = "phoneId";

    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneId);
    final List<DaasPhoneDto> phonesToBuy = null;

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> TransformOrdersUtils.transformNewOrderDto(newOrder, phonesToBuy));

    final String expectedMessage = "'phonesToBuy' must be not null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void transformNewOrderDto_phonesToBuy_is_empty_must_throws_illegalArgumentException() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";
    final String phoneId = "phoneId";

    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneId);
    final List<DaasPhoneDto> phonesToBuy = Collections.emptyList();

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> TransformOrdersUtils.transformNewOrderDto(newOrder, phonesToBuy));

    final String expectedMessage = "'phonesToBuy' must be not empty";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }



  @Test
  void transformNewOrderDto_runsOk() {
    final String phoneId = "phoneId";
    final String name = "name";
    final String description = "description";
    final String imageURL = "imageURL";
    final Double price = 10.0;

    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneId);

    final List<DaasPhoneDto> phonesToBuy =
        Arrays.asList(createDaasPhoneDto(phoneId, name, description, imageURL, price));

    final DaasNewOrderDto actual = TransformOrdersUtils.transformNewOrderDto(newOrder, phonesToBuy);

    assertNotNull(actual);
    assertNotNull(actual.getCustomerData());

    assertEquals(newOrder.getCustomerData().getName(), actual.getCustomerData().getName());
    assertEquals(newOrder.getCustomerData().getSurname(), actual.getCustomerData().getSurname());
    assertEquals(newOrder.getCustomerData().getEmail(), actual.getCustomerData().getEmail());

    assertNotNull(actual.getPhoneListToBuy());
    assertEquals(1, actual.getPhoneListToBuy().size());

    assertEquals(phonesToBuy.get(0).getPhoneId(), actual.getPhoneListToBuy().get(0).getPhoneId());
    assertEquals(phonesToBuy.get(0).getName(), actual.getPhoneListToBuy().get(0).getName());
    assertEquals(phonesToBuy.get(0).getPrice(), actual.getPhoneListToBuy().get(0).getPrice());

    assertNotNull(actual.getTotalPrice());

  }



}
