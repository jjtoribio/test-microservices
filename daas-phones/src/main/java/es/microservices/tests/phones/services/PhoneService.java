package es.microservices.tests.phones.services;

import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhoneService {

  Mono<Long> countAllPhones();

  Flux<PhoneDto> findAllPhones();

  Mono<PhoneDto> findById(String phoneId);

  Mono<PhoneDto> createPhone(NewPhoneDto phone);

}
