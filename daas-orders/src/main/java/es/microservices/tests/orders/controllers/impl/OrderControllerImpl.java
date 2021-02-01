package es.microservices.tests.orders.controllers.impl;

import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import es.microservices.tests.orders.controllers.OrderController;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderListDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.exceptions.ResourceNotFoundException;
import es.microservices.tests.orders.services.OrderService;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/orders")
@Validated
public class OrderControllerImpl implements OrderController {

  private final OrderService service;

  public OrderControllerImpl(final OrderService service) {
    Assert.notNull(service, "'service' must not be null");
    this.service = service;
  }

  @Override
  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Mono<OrderListDto>> getAllOrders(
      @RequestParam(value = "pageSize", required = false,
          defaultValue = "10") final Integer pageSize,
      @RequestParam(value = "page", required = false, defaultValue = "1") final Integer page) {
    final int innerPageSize = prepareDefaultValue(pageSize, 1);
    final int innerPage = prepareDefaultValue(page, 1);

    final Mono<Long> count = this.service.countAllOrders().log();
    final Mono<List<OrderDto>> phones =
        this.service.findAllOrders().log().skip(calculateSkipRecords(innerPageSize, innerPage))
            .take(innerPageSize).log().collectList();

    final Mono<OrderListDto> catalog = Mono.zip(phones, count).flatMap(
        tuple2 -> Mono.create(sink -> sink.success(OrderListDto.builder().page(innerPage)
            .pageSize(innerPageSize).orders(tuple2.getT1()).totalCount(tuple2.getT2()).build())));
    return ResponseEntity.ok(catalog);
  }



  @Override
  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Mono<OrderDto>> createOrder(
      @Valid @RequestBody @NotNull final NewOrderDto newOrder) {
    final Mono<NewOrderDto> monoNewOrder = Mono.just(newOrder);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(monoNewOrder.flatMap(this.service::createOrder));
  }

  @Override
  @GetMapping(value = "/{orderId}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Mono<OrderDto>> getOrder(
      @PathVariable("orderId") @NotBlank final String orderId) {
    return ResponseEntity.ok(
        this.service.findById(orderId).switchIfEmpty(Mono.error(new ResourceNotFoundException())));
  }

  private static int prepareDefaultValue(final Integer limit, final int defaultValue) {
    return Objects.nonNull(limit) ? limit.intValue() : defaultValue;
  }

  private static int calculateSkipRecords(final Integer innerPageSize, final Integer innerPage) {
    return innerPageSize.intValue() * (innerPage.intValue() - 1);
  }

}
