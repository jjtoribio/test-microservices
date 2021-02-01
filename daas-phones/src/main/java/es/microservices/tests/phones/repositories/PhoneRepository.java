package es.microservices.tests.phones.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import es.microservices.tests.phones.entities.PhoneEntity;

@Repository
public interface PhoneRepository extends ReactiveMongoRepository<PhoneEntity, String> {
  
}
