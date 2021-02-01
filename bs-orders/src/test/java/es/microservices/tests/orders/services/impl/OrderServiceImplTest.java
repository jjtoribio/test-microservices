package es.microservices.tests.orders.services.impl;

import static es.microservices.tests.orders.configurations.utils.TestFeatures.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import es.microservices.tests.orders.clients.OrderClient;
import es.microservices.tests.orders.clients.PhoneClient;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.PhoneOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasPhoneOrderDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneCatalogDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneDto;
import es.microservices.tests.orders.exceptions.CreatingOrderException;
import es.microservices.tests.orders.exceptions.PhoneRequestedListEmptyException;
import es.microservices.tests.orders.exceptions.PhoneRequestedNotFound;
import es.microservices.tests.orders.exceptions.RetrievingPhoneException;
import es.microservices.tests.orders.services.OrderService;

class OrderServiceImplTest {

  private PhoneClient phoneClient;
  private OrderClient orderClient;

  private OrderService service;

  @BeforeEach
  public void beforeEach() {
    this.phoneClient = mock(PhoneClient.class);
    this.orderClient = mock(OrderClient.class);
    this.service = new OrderServiceImpl(phoneClient, orderClient);
  }

  @Test
  void phoneClient_isNull() {
    final PhoneClient phoneClient = null;
    final OrderClient orderClient = this.orderClient;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new OrderServiceImpl(phoneClient, orderClient);
    });

    final String expectedMessage = "'phoneClient' must be not null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void orderClient_isNull() {
    final PhoneClient phoneClient = this.phoneClient;
    final OrderClient orderClient = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new OrderServiceImpl(phoneClient, orderClient);
    });

    final String expectedMessage = "'orderClient' must be not null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void newOrder_is_null_then_throws_IllegalArgumentException() {
    final NewOrderDto newOrder = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      this.service.createOrder(newOrder);
    });

    final String expectedMessage = "'newOrder' must be not null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void newOrder_emptyPhoneIdList() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final NewOrderDto newOrder = createNewOrderDto(customerName, customerSurname, customerEmail);

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));   
    
    Exception exception = assertThrows(PhoneRequestedListEmptyException.class, () -> {
      this.service.createOrder(newOrder);
    });

    final String expectedMessage = "Error. The list of phone identifiers is empty";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));
  }

  @Test
  void newOrder_call_to_phoneClient_returns_error_400() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";
    final String[] phoneIds = new String[] {"phoneId1", "phoneId2"};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final HttpStatus status = HttpStatus.BAD_REQUEST;
    doThrow(new HttpClientErrorException(status)).when(this.phoneClient).getPhoneData(anyInt(), anyInt());

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    Exception exception = assertThrows(RetrievingPhoneException.class, () -> {
      this.service.createOrder(newOrder);
    });

    final String expectedMessage = "An error occurred while retrieving the phone catalog";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

  }


  @Test
  void newOrder_call_to_phoneClient_returns_catalog_but_a_requested_phoneId_is_missing() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final String phoneId1 = "phoneId1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final String phoneImageURL1 = "phoneImageURL1";
    final Double phonePrice1 = 100.0;

    final String phoneId2 = "phoneId2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final String phoneImageURL2 = "phoneImageURL2";

    final Double phonePrice2 = 600.0;

    final String phoneId3 = "phoneId3";

    final String[] phoneIds = new String[] {phoneId1, phoneId3};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final Integer page = 1;
    final Integer pageSize = 10;
    final Long totalCount = 2l;
    final DaasPhoneDto phone1 =
        createDaasPhoneDto(phoneId1, phoneName1, phoneDescription1, phoneImageURL1, phonePrice1);
    final DaasPhoneDto phone2 =
        createDaasPhoneDto(phoneId2, phoneName2, phoneDescription2, phoneImageURL2, phonePrice2);

    final DaasPhoneCatalogDto catalog =
        createDaasPhoneCatalogDto(page, pageSize, totalCount, phone1, phone2);

    doReturn(catalog).when(this.phoneClient).getPhoneData(anyInt(), anyInt());

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    Exception exception = assertThrows(PhoneRequestedNotFound.class, () -> {
      this.service.createOrder(newOrder);
    });

    final String expectedMessage = "Cannot continue with order creation. The following phone identifiers have not been found: phoneId3";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

  }


  @Test
  void newOrder_call_to_orderClient_returns_an_error_creating_order() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final String phoneId1 = "phoneId1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final String phoneImageURL1 = "phoneImageURL1";
    final Double phonePrice1 = 100.0;

    final String phoneId2 = "phoneId2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final String phoneImageURL2 = "phoneImageURL2";

    final Double phonePrice2 = 600.0;

    final String[] phoneIds = new String[] {phoneId1, phoneId2};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final Integer page = 1;
    final Integer pageSize = 10;
    final Long totalCount = 2l;
    final DaasPhoneDto phone1 =
        createDaasPhoneDto(phoneId1, phoneName1, phoneDescription1, phoneImageURL1, phonePrice1);
    final DaasPhoneDto phone2 =
        createDaasPhoneDto(phoneId2, phoneName2, phoneDescription2, phoneImageURL2, phonePrice2);

    final DaasPhoneCatalogDto catalog =
        createDaasPhoneCatalogDto(page, pageSize, totalCount, phone1, phone2);

    final HttpStatus status = HttpStatus.BAD_REQUEST;

    doReturn(catalog).when(this.phoneClient).getPhoneData(anyInt(), anyInt());

    doThrow(new HttpClientErrorException(status)).when(this.orderClient).createOrder(any(DaasNewOrderDto.class));

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    Exception exception = assertThrows(CreatingOrderException.class, () -> {
      this.service.createOrder(newOrder);
    });

    final String expectedMessage = "An error occurred creating the order";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, times(1)).createOrder(any(DaasNewOrderDto.class));

  }

  @Test
  void createOrderOk() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";

    final String phoneId1 = "phoneId1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final String phoneImageURL1 = "phoneImageURL1";
    final Double phonePrice1 = 100.0;

    final String phoneId2 = "phoneId2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final String phoneImageURL2 = "phoneImageURL2";

    final Double phonePrice2 = 600.0;

    final String[] phoneIds = new String[] {phoneId1, phoneId2};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final Integer page = 1;
    final Integer pageSize = 10;
    final Long totalCount = 2l;
    final DaasPhoneDto phone1 =
        createDaasPhoneDto(phoneId1, phoneName1, phoneDescription1, phoneImageURL1, phonePrice1);
    final DaasPhoneDto phone2 =
        createDaasPhoneDto(phoneId2, phoneName2, phoneDescription2, phoneImageURL2, phonePrice2);

    final DaasPhoneCatalogDto catalog =
        createDaasPhoneCatalogDto(page, pageSize, totalCount, phone1, phone2);

    final String orderId = "orderId";

    final DaasPhoneOrderDto daasPhoneOrder1 =
        createDaasPhoneOrderDto(phoneId1, phoneName1, phonePrice1);
    final DaasPhoneOrderDto daasPhoneOrder2 =
        createDaasPhoneOrderDto(phoneId2, phoneName2, phonePrice2);

    final DaasOrderDto orderDto = createDaasOrderDto(orderId, customerName, customerSurname,
        customerEmail, phonePrice1 + phonePrice2, daasPhoneOrder1, daasPhoneOrder2);

    final PhoneOrderDto phoneOrder1 = createPhoneOrderDto(phoneId1, phoneName1, phonePrice1);
    final PhoneOrderDto phoneOrder2 = createPhoneOrderDto(phoneId2, phoneName2, phonePrice2);
    final OrderDto orderDtoExpected = createOrderDto(orderId, customerName, customerSurname,
        customerEmail, phonePrice1 + phonePrice2, phoneOrder1, phoneOrder2);

    doReturn(catalog).when(this.phoneClient).getPhoneData(anyInt(), anyInt());
    doReturn(orderDto).when(this.orderClient).createOrder(any(DaasNewOrderDto.class));

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    final OrderDto actualOrder = this.service.createOrder(newOrder);
    assertNotNull(actualOrder);
    assertEquals(orderDtoExpected, actualOrder);

    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, times(1)).createOrder(any(DaasNewOrderDto.class));

  }



}
