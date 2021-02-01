package es.microservices.tests.orders.services;

import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;

@FunctionalInterface
public interface OrderService {

  OrderDto createOrder(NewOrderDto newOrder);
}
