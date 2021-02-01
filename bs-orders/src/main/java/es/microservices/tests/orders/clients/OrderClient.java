package es.microservices.tests.orders.clients;

import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;

@FunctionalInterface
public interface OrderClient {

  DaasOrderDto createOrder(final DaasNewOrderDto newOrder);
}
