package es.microservices.tests.orders.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.entities.OrderEntity;
import es.microservices.tests.orders.mappers.Mapper;
import es.microservices.tests.orders.mappers.impl.NewOrderDto2OrderEntityMapper;
import es.microservices.tests.orders.mappers.impl.OrderEntity2OrderDtoMapper;
import es.microservices.tests.orders.repositories.OrderRepository;
import es.microservices.tests.orders.services.OrderService;
import es.microservices.tests.orders.services.impl.OrderServiceImpl;

@Configuration
public class GlobalConfig {

  @Bean
  public Mapper<NewOrderDto, OrderEntity> dto2EntityMapper() {
    return new NewOrderDto2OrderEntityMapper();
  }

  @Bean
  public Mapper<OrderEntity, OrderDto> entity2DtoMapper() {
    return new OrderEntity2OrderDtoMapper();
  }

  @Bean
  public OrderService orderService(final OrderRepository repository,
      final Mapper<NewOrderDto, OrderEntity> dto2EntityMapper,
      final Mapper<OrderEntity, OrderDto> entity2DtoMapper) {
    return new OrderServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }


}
