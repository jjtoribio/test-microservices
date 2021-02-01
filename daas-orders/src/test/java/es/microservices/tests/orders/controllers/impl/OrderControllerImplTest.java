package es.microservices.tests.orders.controllers.impl;

import static es.microservices.tests.orders.features.TestFeatures.createCollection;
import static es.microservices.tests.orders.features.TestFeatures.createCustomerDto;
import static es.microservices.tests.orders.features.TestFeatures.createPhoneData;
import static es.microservices.tests.orders.features.TestFeatures.createPhoneDto;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import es.microservices.tests.orders.configurations.ErrorManagementConfig;
import es.microservices.tests.orders.configurations.GlobalConfig;
import es.microservices.tests.orders.controllers.handler.SpecificControllerAdvice;
import es.microservices.tests.orders.dtos.CustomerDto;
import es.microservices.tests.orders.dtos.ErrorResponse;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.OrderListDto;
import es.microservices.tests.orders.dtos.PhoneDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.entities.OrderEntity.PhoneData;
import es.microservices.tests.orders.features.TestFeatures;
import es.microservices.tests.orders.repositories.OrderRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderControllerImplTest.InnerConfiguration.class})
@WebFluxTest(controllers = {OrderControllerImpl.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient(timeout = "300000")
public class OrderControllerImplTest {

  private static final String URL = "/orders";

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private OrderRepository repository;

  @BeforeAll
  public static void beforeAll() {
    Locale.setDefault(Locale.US);
  }
  
  @BeforeEach
  public void beforeEach() {
    // BlockHound.install();
    Mockito.clearInvocations(this.repository);
    
  }

  @Test
  void testGetOrderList_findAll_returnsOk_withOut_queryParameters() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 1l;

    final int returnedOrdersExpected = 1;

    final String orderId = UUID.randomUUID().toString();
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

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
    
    final Collection<PhoneData> phoneListToBuyEntity =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));
    
    final OrderEntity entity = TestFeatures.createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuyEntity, totalPrice);
    final OrderDto expectedOrderDto = TestFeatures.createOrderDto(orderId, customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    doReturn(Flux.just(entity)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(OrderListDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getOrders(), notNullValue())
        .value(consume -> consume.getOrders().size(), equalTo(returnedOrdersExpected))
        .value(consume -> consume.getOrders().get(0), equalTo(expectedOrderDto))
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetOrderList_findAll_returns_400_when_page_parameter_is_not_a_number() {
    final String page = "a";
    final int pageSize = 10;

    final String errorMessage = "Type mismatch.";

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isBadRequest()
          .expectBody(ErrorResponse.class)
            .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
            .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
            .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;
    
    // @formatter:on

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();
  }

  @Test
  void testGetOrderList_findAll_returns_400_when_pageSize_parameter_is_not_a_number() {
    final int page = 1;
    final String pageSize = "a";

    final String errorMessage = "Type mismatch.";

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isBadRequest()
          .expectBody(ErrorResponse.class)
            .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
            .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
            .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;
    
    // @formatter:on
    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();
  }

  @Test
  void testGetOrderList_findAll_returnsError() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 0l;

    final String errorMessage = "An error occurred retrieving the data";

    doReturn(Flux.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetOrderList_count_returnsError() {
    final int page = 1;
    final int pageSize = 10;
    
    final String orderId = UUID.randomUUID().toString();
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;
        
    final Collection<PhoneData> phoneListToBuyEntity =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final String errorMessage = "An error occurred counting the data";

    final OrderEntity entity = TestFeatures.createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuyEntity, totalPrice);

    doReturn(Flux.just(entity)).when(this.repository).findAll();
    doReturn(Mono.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetOrderList_returnsEmptyOrderList() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 0l;

    final int returnedOrdersExpected = 0;

    doReturn(Flux.empty()).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(OrderListDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getOrders(), notNullValue())
        .value(consume -> consume.getOrders().size(), equalTo(returnedOrdersExpected));        
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetOrderList_returnsOneOrderList() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 1l;

    final int returnedOrdersExpected = 1;

    final String orderId = UUID.randomUUID().toString();
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

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
    
    final Collection<PhoneData> phoneListToBuyEntity =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final OrderEntity entity = TestFeatures.createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuyEntity, totalPrice);
    final OrderDto expectedOrderDto = TestFeatures.createOrderDto(orderId, customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    doReturn(Flux.just(entity)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(OrderListDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getOrders(), notNullValue())
        .value(consume -> consume.getOrders().size(), equalTo(returnedOrdersExpected))
        .value(consume -> consume.getOrders().get(0), equalTo(expectedOrderDto))        
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetOrderList_returnsTwoOrdersList() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 2l;

    final int returnedOrdersExpected = 2;

    final String orderId_1 = UUID.randomUUID().toString();
    final String customerName_order1 = "customerName_order1";
    final String customerSurname_order1 = "customerSurname_order1";
    final String customerEmail_order1 = "customerEmail_order1";
    
    final String orderId_2 = UUID.randomUUID().toString();
    final String customerName_order2 = "customerName_order2";
    final String customerSurname_order2 = "customerSurname_order2";
    final String customerEmail_order2 = "customerEmail_order2";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice_order1 = 305.33;
    final Double totalPrice_order2 = 205.22;

    final Collection<PhoneDto> phoneListToBuy_order1 =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));
    
    final Collection<PhoneData> phoneListToBuyEntity_order1 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));
    
    final Collection<PhoneDto> phoneListToBuy_order2 =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1));
    
    final Collection<PhoneData> phoneListToBuyEntity_order2 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1));

    final OrderEntity entity1 = TestFeatures.createOrderEntity(orderId_1, customerName_order1, customerSurname_order1, customerEmail_order1, phoneListToBuyEntity_order1, totalPrice_order1) ;
    final OrderDto expectedOrderDto1 = TestFeatures.createOrderDto(orderId_1, customerName_order1, customerSurname_order1, customerEmail_order1, phoneListToBuy_order1, totalPrice_order1) ;

    final OrderEntity entity2 = TestFeatures.createOrderEntity(orderId_2, customerName_order2, customerSurname_order2, customerEmail_order2, phoneListToBuyEntity_order2, totalPrice_order2);
    final OrderDto expectedOrderDto2 = TestFeatures.createOrderDto(orderId_2, customerName_order2, customerSurname_order2, customerEmail_order2, phoneListToBuy_order2, totalPrice_order2);

    doReturn(Flux.just(entity1, entity2)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(OrderListDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getOrders(), notNullValue())
        .value(consume -> consume.getOrders().size(), equalTo(returnedOrdersExpected))
        .value(consume -> consume.getOrders().get(0), equalTo(expectedOrderDto1))
        .value(consume -> consume.getOrders().get(1), equalTo(expectedOrderDto2))        
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetOrderList_returnsTwoOrdersList_pageSize_1_page_1() {
    final int page = 1;
    final int pageSize = 1;
    final Long totalCount = 2l;

    final int returnedOrdersExpected = 1;

    final String orderId_1 = UUID.randomUUID().toString();
    final String customerName_order1 = "customerName_order1";
    final String customerSurname_order1 = "customerSurname_order1";
    final String customerEmail_order1 = "customerEmail_order1";
    
    final String orderId_2 = UUID.randomUUID().toString();
    final String customerName_order2 = "customerName_order2";
    final String customerSurname_order2 = "customerSurname_order2";
    final String customerEmail_order2 = "customerEmail_order2";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice_order1 = 305.33;
    final Double totalPrice_order2 = 205.22;

    final Collection<PhoneDto> phoneListToBuy_order1 =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));
    
    final Collection<PhoneData> phoneListToBuyEntity_order1 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final Collection<PhoneData> phoneListToBuyEntity_order2 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1));

    final OrderEntity entity1 = TestFeatures.createOrderEntity(orderId_1, customerName_order1, customerSurname_order1, customerEmail_order1, phoneListToBuyEntity_order1, totalPrice_order1);
    final OrderEntity entity2 = TestFeatures.createOrderEntity(orderId_2, customerName_order2, customerSurname_order2, customerEmail_order2, phoneListToBuyEntity_order2, totalPrice_order2);

    final OrderDto expectedOrderDto1 = TestFeatures.createOrderDto(orderId_1, customerName_order1, customerSurname_order1, customerEmail_order1, phoneListToBuy_order1, totalPrice_order1);

    doReturn(Flux.just(entity1, entity2)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(OrderListDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getOrders(), notNullValue())
        .value(consume -> consume.getOrders().size(), equalTo(returnedOrdersExpected))
        .value(consume -> consume.getOrders().get(0), equalTo(expectedOrderDto1))
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetOrderList_returnsTwoOrdersList_pageSize_1_page_2() {
    final int page = 2;
    final int pageSize = 1;
    final Long totalCount = 2l;

    final int returnedOrdersExpected = 1;

    final String orderId_1 = UUID.randomUUID().toString();
    final String customerName_order1 = "customerName_order1";
    final String customerSurname_order1 = "customerSurname_order1";
    final String customerEmail_order1 = "customerEmail_order1";
    
    final String orderId_2 = UUID.randomUUID().toString();
    final String customerName_order2 = "customerName_order2";
    final String customerSurname_order2 = "customerSurname_order2";
    final String customerEmail_order2 = "customerEmail_order2";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice_order1 = 305.33;
    final Double totalPrice_order2 = 205.22;
    
    final Collection<PhoneData> phoneListToBuyEntity_order1 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));
    
    final Collection<PhoneDto> phoneListToBuy_order2 =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1));
    
    final Collection<PhoneData> phoneListToBuyEntity_order2 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1));

    final OrderEntity entity1 = TestFeatures.createOrderEntity(orderId_1, customerName_order1, customerSurname_order1, customerEmail_order1, phoneListToBuyEntity_order1, totalPrice_order1);
    final OrderEntity entity2 = TestFeatures.createOrderEntity(orderId_2, customerName_order2, customerSurname_order2, customerEmail_order2, phoneListToBuyEntity_order2, totalPrice_order2);


    final OrderDto expectedOrderDto2 = TestFeatures.createOrderDto(orderId_2, customerName_order2, customerSurname_order2, customerEmail_order2, phoneListToBuy_order2, totalPrice_order2);

    doReturn(Flux.just(entity1, entity2)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(OrderListDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getOrders(), notNullValue())
        .value(consume -> consume.getOrders().size(), equalTo(returnedOrdersExpected))
        .value(consume -> consume.getOrders().get(0), equalTo(expectedOrderDto2))
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }



  @Test
  void testGetOrderById_orderId_found() {
    final String orderId = UUID.randomUUID().toString();
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;
    
    final Collection<PhoneData> phoneListToBuyEntity =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final OrderEntity entity = TestFeatures.createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuyEntity, totalPrice);

    doReturn(Mono.just(entity)).when(this.repository).findById(anyString());

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL.concat("/{orderId}"))
            .build(orderId))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(OrderDto.class)
        .value(consume -> consume.getOrderId(), equalTo(orderId))
        .value(consume -> consume.getCustomerData().getName(), equalTo(customerName))
        .value(consume -> consume.getCustomerData().getSurname(), equalTo(customerSurname))
        .value(consume -> consume.getCustomerData().getEmail(), equalTo(customerEmail))
        .value(consume -> consume.getPhoneListToBuy().size(), equalTo(2))
        .value(consume -> consume.getPhoneListToBuy().get(0).getPhoneId(), equalTo(phoneId_1))
        .value(consume -> consume.getPhoneListToBuy().get(0).getName(), equalTo(phoneName_1))
        .value(consume -> consume.getPhoneListToBuy().get(0).getPrice(), equalTo(phonePrice_1))
        .value(consume -> consume.getPhoneListToBuy().get(1).getPhoneId(), equalTo(phoneId_2))
        .value(consume -> consume.getPhoneListToBuy().get(1).getName(), equalTo(phoneName_2))
        .value(consume -> consume.getPhoneListToBuy().get(1).getPrice(), equalTo(phonePrice_2))
        .value(consume -> consume.getTotalPrice(), equalTo(totalPrice))        
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void testGetOrderById_orderId_notFound() {
    final String orderId = "1";

    final String errorMessage = "Resource not found!";

    doReturn(Mono.empty()).when(this.repository).findById(anyString());

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL.concat("/{orderId}"))
            .build(orderId))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isNotFound()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.NOT_FOUND.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;    
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void testGetOrderById_findById_returns_error() {
    final String orderId = "1";

    final String errorMessage = "An error ocurrs finding the Order";

    doReturn(Mono.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).findById(anyString());

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL.concat("/{orderId}"))
            .build(orderId))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;    
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }


  @Test
  void testPostAddOrderToCatalog_save_method_returns_error() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

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
    
    final NewOrderDto newOrder =
        TestFeatures.createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    final String errorMessage = "An error ocurrs saving the new Order";

    doReturn(Mono.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).save(any(OrderEntity.class));

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, times(1)).save(any(OrderEntity.class));
  }

  @Test
  void testPostAddOrderToCatalog_customerDto_isNull() {
    final CustomerDto customerDto = null;

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData: must not be null";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }

  @Test
  void testPostAddOrderToCatalog_customerDto_name_isNul() {    
    final String customerName = null;
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    
    final CustomerDto customerDto = new CustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData.name: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_customerDto_name_isEmpty() {    
    final String customerName = " ";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    
    final CustomerDto customerDto = new CustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData.name: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  
  
  
  @Test
  void testPostAddOrderToCatalog_customerDto_surname_isNul() {    
    final String customerName = "customerName";
    final String customerSurname = null;
    final String customerEmail = "email@email.com";
    
    final CustomerDto customerDto = new CustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData.surname: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_customerDto_customerSurname_isEmpty() {    
    final String customerName = "customerName";
    final String customerSurname = " ";
    final String customerEmail = "email@email.com";
    
    final CustomerDto customerDto = new CustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData.surname: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_customerDto_customerEmail_isNul() {    
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = null;
    
    final CustomerDto customerDto = new CustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData.email: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_customerDto_customerEmail_isEmpty() {    
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = " ";
    
    final CustomerDto customerDto = new CustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData.email: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(),  containsString(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_customerDto_customerEmail_isInvalidEmail() {    
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "customerEmail";
    
    final CustomerDto customerDto = new CustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice = 305.33;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));    
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);

    final String errorMessage = "customerData.email: must be a well-formed email address";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_isNull() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final List<PhoneDto> phoneListToBuy = null;
    
    final Double totalPrice = 305.33;
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy: must not be empty";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_isEmpty() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final List<PhoneDto> phoneListToBuy = Collections.emptyList();
    
    final Double totalPrice = 305.33;

    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy: must not be empty";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_with_a_null_element() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final List<PhoneDto> phoneListToBuy = new ArrayList<>();
    phoneListToBuy.add(null);

    final Double totalPrice = 305.33;

    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy[0]: must not be null";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_with_phone_wiht_null_phoneId() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId = null;
    final String phoneName = "phoneName";
    final Double phonePrice = 205.22;

    final List<PhoneDto> phoneListToBuy =
        createCollection(new PhoneDto(phoneId, phoneName, phonePrice));

    final Double totalPrice =  205.22;

    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy[0].phoneId: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_with_phone_wiht_phoneId_empty() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId = " ";
    final String phoneName = "phoneName";
    final Double phonePrice = 205.22;

    final List<PhoneDto> phoneListToBuy =
        createCollection(new PhoneDto(phoneId, phoneName, phonePrice));

    final Double totalPrice =  205.22;

    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy[0].phoneId: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_with_phone_with_phoneName_null() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId = "1";
    final String phoneName = null;
    final Double phonePrice = 205.22;

    final List<PhoneDto> phoneListToBuy =
        createCollection(new PhoneDto(phoneId, phoneName, phonePrice));

    final Double totalPrice =  205.22;

    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy[0].name: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_with_phone_with_phoneName_empty() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId = "1";
    final String phoneName = " ";
    final Double phonePrice = 205.22;

    final List<PhoneDto> phoneListToBuy =
        createCollection(new PhoneDto(phoneId, phoneName, phonePrice));

    final Double totalPrice =  205.22;

    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy[0].name: must not be blank";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  @Test
  void testPostAddOrderToCatalog_phoneListToBuy_with_phone_with_phonePrice_null() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId = "1";
    final String phoneName = "phoneName";
    final Double phonePrice = null;

    final List<PhoneDto> phoneListToBuy =
        createCollection(new PhoneDto(phoneId, phoneName, phonePrice));

    final Double totalPrice =  205.22;

    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "phoneListToBuy[0].price: must not be null";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }
  
  
  
  
  
  
  
  
  
  @Test
  void testPostAddOrderToCatalog_totalPrice_isNull() {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final CustomerDto customerDto = createCustomerDto(customerName, customerSurname, customerEmail);

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final List<PhoneDto> phoneListToBuy =
        createCollection(createPhoneDto(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneDto(phoneId_2, phoneName_2, phonePrice_2));
    
    final Double totalPrice = null;
    
    final NewOrderDto newOrder = new NewOrderDto(customerDto, phoneListToBuy, totalPrice);    

    final String errorMessage = "totalPrice: must not be null";

    verify(this.repository, never()).save(any(OrderEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(OrderEntity.class));
  }

  @Test
  void testPostAddOrderToCatalog_createOk() {
    final String orderId = UUID.randomUUID().toString();
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

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
    
    final Collection<PhoneData> phoneListToBuyEntity =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));

    final NewOrderDto newOrder =
        TestFeatures.createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);
    
    final OrderEntity entity = TestFeatures.createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuyEntity, totalPrice);

    doReturn(Mono.just(entity)).when(this.repository).save(any(OrderEntity.class));

    verify(this.repository, never()).save(any(OrderEntity.class));
 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newOrder)
        .exchange()
          .expectStatus()
            .isCreated()
        .expectBody(OrderDto.class)
        .value(consume -> consume.getOrderId(), notNullValue())
        .value(consume -> consume.getCustomerData().getName(), equalTo(customerName))
        .value(consume -> consume.getCustomerData().getSurname(), equalTo(customerSurname))
        .value(consume -> consume.getCustomerData().getEmail(), equalTo(customerEmail))
        .value(consume -> consume.getPhoneListToBuy().size(), equalTo(2))
        .value(consume -> consume.getPhoneListToBuy().get(0).getPhoneId(), equalTo(phoneId_1))
        .value(consume -> consume.getPhoneListToBuy().get(0).getName(), equalTo(phoneName_1))
        .value(consume -> consume.getPhoneListToBuy().get(0).getPrice(), equalTo(phonePrice_1))
        .value(consume -> consume.getPhoneListToBuy().get(1).getPhoneId(), equalTo(phoneId_2))
        .value(consume -> consume.getPhoneListToBuy().get(1).getName(), equalTo(phoneName_2))
        .value(consume -> consume.getPhoneListToBuy().get(1).getPrice(), equalTo(phonePrice_2))
        .value(consume -> consume.getTotalPrice(), equalTo(totalPrice)) 
        ;    
    // @formatter:on

    verify(this.repository, times(1)).save(any(OrderEntity.class));
  }



  @TestConfiguration
  @EnableAutoConfiguration
  @Import({OrderControllerImpl.class, GlobalConfig.class, ErrorManagementConfig.class,
      SpecificControllerAdvice.class})
  static class InnerConfiguration {

    @Bean
    public OrderRepository repository() {
      return mock(OrderRepository.class);
    }

    @Bean
    public Tracer tracer() {
      final List<Span> spans = new ArrayList<>();
      Reporter<Span> reporter = spans::add;
      CurrentTraceContext currentTraceContext = CurrentTraceContext.Default.inheritable();
      currentTraceContext
          .newScope(TraceContext.newBuilder().traceIdHigh(-1L).traceId(1L).spanId(1L).build());
      Tracing tracing = Tracing.newBuilder().currentTraceContext(currentTraceContext)
          .localServiceName("localServiceName").spanReporter(reporter).sampler(Sampler.NEVER_SAMPLE)
          .build();
      return tracing.tracer();
    }

  }
}
