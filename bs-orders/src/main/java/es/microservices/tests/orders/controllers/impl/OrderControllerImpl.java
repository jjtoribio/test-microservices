package es.microservices.tests.orders.controllers.impl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.microservices.tests.orders.controllers.OrderController;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.services.OrderService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/orders")
@Validated
@Slf4j
public class OrderControllerImpl implements OrderController {

  private final OrderService service;
  private final ObjectMapper objectMapper;

  public OrderControllerImpl(final OrderService service, final ObjectMapper objectMapper) {
    Assert.notNull(service, "'service' must not be null");
    this.service = service;
    this.objectMapper = objectMapper;
  }

  @Override
  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<OrderDto> createOrder(
      @Valid @RequestBody @NotNull final NewOrderDto newOrder) {

    final OrderDto orderDto = this.service.createOrder(newOrder);
    this.logResponse(orderDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
  }

  private void logResponse(OrderDto order) {
    try {
      log.info(this.objectMapper.writeValueAsString(order));
    } catch (JsonProcessingException e) {
      log.error("An error occurred serializing the object for screen printing", e);
    }
  }

}
