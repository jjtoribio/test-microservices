package es.microservices.tests.orders.services;

import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

  Mono<Long> countAllOrders();

  Flux<OrderDto> findAllOrders();

  Mono<OrderDto> findById(String orderId);

  Mono<OrderDto> createOrder(NewOrderDto order);

}
