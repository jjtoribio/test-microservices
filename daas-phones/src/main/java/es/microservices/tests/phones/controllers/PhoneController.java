package es.microservices.tests.phones.controllers;

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
import es.microservices.tests.phones.dtos.ErrorResponse;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneCatalogDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import reactor.core.publisher.Mono;

@RequestMapping("/phones")
public interface PhoneController {

  @Operation(summary = "Add a new phone to the catalog",
      description = "Add a new phone to the catalog", tags = {"Phones Catalog"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Phone created",
          content = @Content(schema = @Schema(implementation = PhoneDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<Mono<PhoneDto>> addPhoneToCatalog(
      @Parameter(in = ParameterIn.DEFAULT, description = "the body request", required = true,
          schema = @Schema()) @Valid @RequestBody @NotNull final NewPhoneDto newPhone);


  @Operation(summary = "Retrieve a phone from the catalog by its id.",
      description = "Retrieve a phone from the catalog by its id.", tags = {"Phones Catalog"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful operation",
          content = @Content(schema = @Schema(implementation = PhoneDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @GetMapping(value = "/{phoneId}", produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<Mono<PhoneDto>> getPhone(
      @Parameter(in = ParameterIn.PATH, description = "The phone identifier", required = true,
          schema = @Schema()) @PathVariable("phoneId") @NotBlank String phoneId);


  @Operation(summary = "Retrieves all the phones in the catalog",
      description = "Retrieves all the phones in the catalog", tags = {"Phone Catalog"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful operation",
          content = @Content(schema = @Schema(implementation = PhoneCatalogDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

      @ApiResponse(responseCode = "500", description = "Internal Server Error",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<Mono<PhoneCatalogDto>> getPhonesCatalog(
      @Parameter(in = ParameterIn.QUERY,
          description = "Indicates how many results the query should return at most.",
          schema = @Schema(defaultValue = "1"), required = false) @RequestParam(value = "pageSize",
              required = false, defaultValue = "10") Integer pageSize,
      @Parameter(in = ParameterIn.QUERY, 
          description = "The (zero-based) offset of the first item in the collection to return",
          schema = @Schema(defaultValue = "1"), required = false) @RequestParam(value = "page",
              required = false, defaultValue = "1") Integer page);

}
