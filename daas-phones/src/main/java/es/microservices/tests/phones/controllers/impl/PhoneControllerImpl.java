package es.microservices.tests.phones.controllers.impl;

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
import es.microservices.tests.phones.controllers.PhoneController;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneCatalogDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.exceptions.ResourceNotFoundException;
import es.microservices.tests.phones.services.PhoneService;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/phones")
@Validated
public class PhoneControllerImpl implements PhoneController {

  private final PhoneService service;

  public PhoneControllerImpl(final PhoneService service) {
    Assert.notNull(service, "'service' must not be null");
    this.service = service;
  }

  @Override
  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Mono<PhoneCatalogDto>> getPhonesCatalog(
      @RequestParam(value = "pageSize", required = false,
          defaultValue = "10") final Integer pageSize,
      @RequestParam(value = "page", required = false, defaultValue = "1") final Integer page) {
    final int innerPageSize = prepareDefaultValue(pageSize, 1);
    final int innerPage = prepareDefaultValue(page, 1);

    final Mono<Long> count = this.service.countAllPhones().log();
    final Mono<List<PhoneDto>> phones =
        this.service.findAllPhones().log().skip(calculateSkipRecords(innerPageSize, innerPage))
            .take(innerPageSize).log().collectList();

    final Mono<PhoneCatalogDto> catalog = Mono.zip(phones, count).flatMap(
        tuple2 -> Mono.create(sink -> sink.success(PhoneCatalogDto.builder().page(innerPage)
            .pageSize(innerPageSize).phones(tuple2.getT1()).totalCount(tuple2.getT2()).build())));
    return ResponseEntity.ok(catalog);
  }



  @Override
  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Mono<PhoneDto>> addPhoneToCatalog(
      @Valid @RequestBody @NotNull final NewPhoneDto newPhone) {
    final Mono<NewPhoneDto> monoNewPhone = Mono.just(newPhone);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(monoNewPhone.flatMap(this.service::createPhone));
  }

  @Override
  @GetMapping(value = "/{phoneId}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Mono<PhoneDto>> getPhone(
      @PathVariable("phoneId") @NotBlank final String phoneId) {
    return ResponseEntity.ok(
        this.service.findById(phoneId).switchIfEmpty(Mono.error(new ResourceNotFoundException())));
  }

  private static int prepareDefaultValue(final Integer limit, final int defaultValue) {
    return Objects.nonNull(limit) ? limit.intValue() : defaultValue;
  }

  private static int calculateSkipRecords(final Integer innerPageSize, final Integer innerPage) {
    return innerPageSize.intValue() * (innerPage.intValue() - 1);
  }

}
