package es.microservices.tests.orders.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import es.microservices.tests.orders.entities.OrderEntity;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<OrderEntity, String> {
  
}
