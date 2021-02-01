package es.microservices.tests.orders.services.impl;

import static es.microservices.tests.orders.features.TestFeatures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Collection;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.PhoneDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.entities.OrderEntity.PhoneData;
import es.microservices.tests.orders.mappers.Mapper;
import es.microservices.tests.orders.mappers.impl.NewOrderDto2OrderEntityMapper;
import es.microservices.tests.orders.mappers.impl.OrderEntity2OrderDtoMapper;
import es.microservices.tests.orders.repositories.OrderRepository;
import es.microservices.tests.orders.services.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderServiceImplTest {

  private OrderService service;
  private OrderRepository repository;

  private Mapper<NewOrderDto, OrderEntity> dto2EntityMapper;
  private Mapper<OrderEntity, OrderDto> entity2DtoMapper;


  @BeforeEach
  public void before() {
    // BlockHound.install();
    this.dto2EntityMapper = new NewOrderDto2OrderEntityMapper();
    this.entity2DtoMapper = new OrderEntity2OrderDtoMapper();

    this.repository = mock(OrderRepository.class);
    this.service =
        new OrderServiceImpl(this.repository, this.dto2EntityMapper, this.entity2DtoMapper);
  }

  @Test
  void repository_isNull() {
    final OrderRepository repository = null;
    final Mapper<NewOrderDto, OrderEntity> dto2EntityMapper = this.dto2EntityMapper;
    final Mapper<OrderEntity, OrderDto> entity2DtoMapper = this.entity2DtoMapper;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new OrderServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
    });

    final String expectedMessage = "'repository' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void dto2EntityMapper_isNull() {
    final OrderRepository repository = this.repository;
    final Mapper<NewOrderDto, OrderEntity> dto2EntityMapper = null;
    final Mapper<OrderEntity, OrderDto> entity2DtoMapper = this.entity2DtoMapper;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new OrderServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
    });

    final String expectedMessage = "'dto2EntityMapper' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void entity2DtoMapper_isNull() {
    final OrderRepository repository = this.repository;
    final Mapper<NewOrderDto, OrderEntity> dto2EntityMapper = this.dto2EntityMapper;
    final Mapper<OrderEntity, OrderDto> entity2DtoMapper = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new OrderServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
    });

    final String expectedMessage = "'entity2DtoMapper' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void findAllOrders_returnEmptyFlux() {
    doReturn(Flux.empty()).when(this.repository).findAll();

    Flux<OrderDto> fluxOrderCatalog = this.service.findAllOrders();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxOrderCatalog)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllOrders_throwsError() {
    doThrow(new DataRetrievalFailureException("error")).when(this.repository).findAll();

    Flux<OrderDto> fluxOrderCatalog = this.service.findAllOrders();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxOrderCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllOrders_returnsFluxError() {
    doReturn(Flux.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .findAll();

    Flux<OrderDto> fluxOrderCatalog = this.service.findAllOrders();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxOrderCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllOrders_returnOneElementFlux() {
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
    
    final OrderEntity orderEntity =
        createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);
    
    doReturn(Flux.just(orderEntity)).when(this.repository).findAll();

    Flux<OrderDto> fluxOrderCatalog = this.service.findAllOrders();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxOrderCatalog)
      .assertNext(orderDto -> evaluateResult(orderEntity, orderDto))
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllOrders_returnTwoElementsFlux() {
    final String orderId_1 = "orderId_1";
    final String customerName_1 = "customerName_1";
    final String customerSurname_1 = "customerSurname_1";
    final String customerEmail_1 = "customerEmail_1";
    
    final String orderId_2 = "orderId_2";
    final String customerName_2 = "customerName_2";
    final String customerSurname_2 = "customerSurname_2";
    final String customerEmail_2 = "customerEmail_2";

    final String phoneId_1 = "1";
    final String phoneName_1 = "phoneName_1";
    final double phonePrice_1 = 205.22;

    final String phoneId_2 = "2";
    final String phoneName_2 = "phoneName_2";
    final double phonePrice_2 = 100.11;

    final Double totalPrice_order1 = 305.33;
    final Double totalPrice_order2 = 205.22;
    
    final Collection<PhoneData> phoneListToBuy_order1 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));
    
    final Collection<PhoneData> phoneListToBuy_order2 =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1));
    
    final OrderEntity orderEntity1 =
        createOrderEntity(orderId_1, customerName_1, customerSurname_1, customerEmail_1, phoneListToBuy_order1, totalPrice_order1);
    
    final OrderEntity orderEntity2 =
        createOrderEntity(orderId_2, customerName_2, customerSurname_2, customerEmail_2, phoneListToBuy_order2, totalPrice_order2);

    doReturn(Flux.just(orderEntity1, orderEntity2)).when(this.repository).findAll();

    Flux<OrderDto> fluxOrderCatalog = this.service.findAllOrders();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxOrderCatalog)
      .assertNext(orderDto -> evaluateResult(orderEntity1, orderDto))
      .assertNext(orderDto -> evaluateResult(orderEntity2, orderDto))
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }



  @Test
  void countAllOrders_returnEmptyMono() {
    doReturn(Mono.empty()).when(this.repository).count();

    final Mono<Long> monoOrderCatalogCount = this.service.countAllOrders();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoOrderCatalogCount)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }

  @Test
  void countAllOrders_throwsError() {
    doThrow(new DataRetrievalFailureException("error")).when(this.repository).count();

    final Mono<Long> monoOrderCatalogCount = this.service.countAllOrders();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoOrderCatalogCount)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }

  @Test
  void countAllOrders_returnsFluxError() {
    doReturn(Mono.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .count();

    final Mono<Long> monoOrderCatalogCount = this.service.countAllOrders();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoOrderCatalogCount)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }

  @Test
  void countAllOrders_returnCounter() {
    doReturn(Mono.just(1l)).when(this.repository).count();

    final Mono<Long> monoOrderCatalogCount = this.service.countAllOrders();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoOrderCatalogCount)
      .expectNext(1l)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }


  @Test
  void findById_returnEmptyMono() {
    final String orderId = "1";
    doReturn(Mono.empty()).when(this.repository).findById(anyString());

    final Mono<OrderDto> monoOrderCatalog = this.service.findById(orderId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoOrderCatalog)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void findById_throwsError() {
    final String orderId = "1";
    doThrow(new DataRetrievalFailureException("error")).when(this.repository).findById(anyString());

    final Mono<OrderDto> monoOrderCatalog = this.service.findById(orderId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoOrderCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void findById_returnsMonoError() {
    final String orderId = "1";
    doReturn(Mono.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .findById(anyString());

    final Mono<OrderDto> monoOrderCatalog = this.service.findById(orderId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoOrderCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void findById_returnTheElement() {
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
    
    final OrderEntity orderEntity =
        createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);
    
    
    doReturn(Mono.just(orderEntity)).when(this.repository).findById(anyString());

    Mono<OrderDto> monoOrderCatalog = this.service.findById(orderId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoOrderCatalog)
    .assertNext(orderDto -> evaluateResult(orderEntity, orderDto))
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }


  @Test
  void createOrder_throwsError() {
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
    
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    doThrow(new DataRetrievalFailureException("error")).when(this.repository)
        .save(any(OrderEntity.class));

    final Mono<OrderDto> monoOrder = this.service.createOrder(newOrder);

    verify(this.repository, never()).save(any(OrderEntity.class));

    // @formatter:off
    StepVerifier.create(monoOrder)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).save(any(OrderEntity.class));
  }

  @Test
  void createOrder_returnsMonoError() {
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
    
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);

    doReturn(Mono.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .save(any(OrderEntity.class));

    final Mono<OrderDto> monoOrder = this.service.createOrder(newOrder);

    verify(this.repository, never()).save(any(OrderEntity.class));

    // @formatter:off
    StepVerifier.create(monoOrder)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).save(any(OrderEntity.class));
  }

  @Test
  void createOrder_createOk() {
    final String orderId = UUID.randomUUID().toString();
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
    
    final Collection<PhoneData> phoneListToBuyEntity =
        createCollection(createPhoneData(phoneId_1, phoneName_1, phonePrice_1),
            createPhoneData(phoneId_2, phoneName_2, phonePrice_2));
    
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneListToBuy, totalPrice);
    final OrderEntity orderEntity =
        createOrderEntity(orderId, customerName, customerSurname, customerEmail, phoneListToBuyEntity, totalPrice);
    
    doReturn(Mono.just(orderEntity)).when(this.repository).save(any(OrderEntity.class));

    Mono<OrderDto> monoOrder = this.service.createOrder(newOrder);

    verify(this.repository, never()).save(any(OrderEntity.class));

    // @formatter:off
    StepVerifier.create(monoOrder)
      .assertNext(orderDto -> evaluateResult(orderEntity, orderDto))
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).save(any(OrderEntity.class));
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

    if (input.getPhoneListToBuy().size() > 1) {
      assertEquals(input.getPhoneListToBuy().get(1).getPhoneId(),
        orderDto.getPhoneListToBuy().get(1).getPhoneId());
      assertEquals(input.getPhoneListToBuy().get(1).getName(),
        orderDto.getPhoneListToBuy().get(1).getName());
      assertEquals(input.getPhoneListToBuy().get(1).getPrice(),
        orderDto.getPhoneListToBuy().get(1).getPrice());
    }
    assertEquals(input.getTotalPrice().doubleValue(), orderDto.getTotalPrice().doubleValue());
  }

}
