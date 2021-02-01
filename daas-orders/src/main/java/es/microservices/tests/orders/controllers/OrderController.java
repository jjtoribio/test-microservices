package es.microservices.tests.orders.controllers;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.microservices.tests.orders.dtos.ErrorResponse;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderListDto;
import es.microservices.tests.orders.dtos.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import reactor.core.publisher.Mono;

@RequestMapping("/orders")
public interface OrderController {

  @Operation(summary = "Create a new order",
      description = "Create a new order", tags = {"Orders"})
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
  ResponseEntity<Mono<OrderDto>> createOrder(
      @Parameter(in = ParameterIn.DEFAULT, description = "the body request", required = true,
          schema = @Schema()) @Valid @RequestBody @NotNull final NewOrderDto newOrder);


  @Operation(summary = "Retrieve an order by its id.",
      description = "Retrieve an order by its id.", tags = {"Orders"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful operation",
          content = @Content(schema = @Schema(implementation = OrderDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @GetMapping(value = "/{orderId}", produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<Mono<OrderDto>> getOrder(
      @Parameter(in = ParameterIn.PATH, description = "The order identifier", required = true,
          schema = @Schema()) @PathVariable("orderId") @NotBlank String orderId);


  @Operation(summary = "Retrieves all orders",
      description = "Retrieves all orders", tags = {"Orders"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful operation",
          content = @Content(schema = @Schema(implementation = OrderListDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

      @ApiResponse(responseCode = "500", description = "Internal Server Error",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<Mono<OrderListDto>> getAllOrders(
      @Parameter(in = ParameterIn.QUERY,
          description = "Indicates how many results the query should return at most.",
          schema = @Schema(defaultValue = "1"), required = false) @RequestParam(value = "pageSize",
              required = false, defaultValue = "10") Integer pageSize,
      @Parameter(in = ParameterIn.QUERY, 
          description = "The (zero-based) offset of the first item in the collection to return",
          schema = @Schema(defaultValue = "1"), required = false) @RequestParam(value = "page",
              required = false, defaultValue = "1") Integer page);

}
