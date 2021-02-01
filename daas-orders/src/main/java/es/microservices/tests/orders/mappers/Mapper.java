package es.microservices.tests.orders.mappers;

import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface Mapper<I, O> {

  O map(final I input);

  default Mono<O> map(final Mono<I> input) {
    Assert.notNull(input, "'input' must not be null");
    return input.flatMap(this::mapToMono);
  }

  default Flux<O> map(final Flux<I> input) {
    Assert.notNull(input, "'input' must not be null");
    return input.flatMap(this::mapToFlux);
  }
  
  default Mono<O> mapToMono(final I input) {
    Assert.notNull(input, "'input' must not be null");
    return  Mono.defer(() -> Mono.just(this.map(input)));
  }

  default Flux<O> mapToFlux(final I input) {
    Assert.notNull(input, "'input' must not be null");
    return Flux.defer(() -> Flux.just(this.map(input)));    
  }
}