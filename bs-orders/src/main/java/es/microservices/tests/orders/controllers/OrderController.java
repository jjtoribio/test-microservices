package es.microservices.tests.orders.controllers;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import es.microservices.tests.orders.dtos.ErrorResponse;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RequestMapping("/orders")
public interface OrderController {

  @Operation(summary = "Create a new order", description = "Create a new order", tags = {"Orders"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Order created",
          content = @Content(schema = @Schema(implementation = OrderDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<OrderDto> createOrder(
      @Parameter(in = ParameterIn.DEFAULT, description = "the body request", required = true,
          schema = @Schema()) @Valid @RequestBody @NotNull final NewOrderDto newOrder);
}
