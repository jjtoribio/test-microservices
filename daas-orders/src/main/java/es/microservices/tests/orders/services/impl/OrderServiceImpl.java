package es.microservices.tests.orders.services.impl;

import org.springframework.util.Assert;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.mappers.Mapper;
import es.microservices.tests.orders.repositories.OrderRepository;
import es.microservices.tests.orders.services.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OrderServiceImpl implements OrderService {

  private final OrderRepository repository;
  private final Mapper<NewOrderDto, OrderEntity> dto2EntityMapper;
  private final Mapper<OrderEntity, OrderDto> entity2DtoMapper;

  public OrderServiceImpl(final OrderRepository repository,
      final Mapper<NewOrderDto, OrderEntity> dto2EntityMapper,
      final Mapper<OrderEntity, OrderDto> entity2DtoMapper) {
    Assert.notNull(repository, "'repository' must not be null");
    Assert.notNull(dto2EntityMapper, "'dto2EntityMapper' must not be null");
    Assert.notNull(entity2DtoMapper, "'entity2DtoMapper' must not be null");
    this.repository = repository;
    this.dto2EntityMapper = dto2EntityMapper;
    this.entity2DtoMapper = entity2DtoMapper;

  }

  @Override
  public Flux<OrderDto> findAllOrders() {    
    // @formatter:off
    return Flux.defer(this.repository::findAll)
        .map(this.entity2DtoMapper::map);
    // @formatter:on

  }
  
  @Override
  public Mono<Long> countAllOrders() {
    return Mono.defer(this.repository::count);    
  }
  
  @Override
  public Mono<OrderDto> findById(String orderId) {
    Assert.hasText(orderId, "'orderId' must not be null or empty");
    return Mono.defer(() -> this.repository.findById(orderId))
        .map(this.entity2DtoMapper::map);    
  }
  
  @Override
  public Mono<OrderDto> createOrder(final NewOrderDto order) {
    Assert.notNull(order, "'order' must not be null");
    return this.dto2EntityMapper.map(Mono.just(order))
        .flatMap(this::saveOrderEntity)
      ;    
  }
  
  private Mono<OrderDto> saveOrderEntity(OrderEntity order) {
    return Mono.defer(() -> this.repository.save(order))
        .map(this.entity2DtoMapper::map);
  }

}
