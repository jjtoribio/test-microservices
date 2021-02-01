package es.microservices.tests.phones.mappers.impl;

import static es.microservices.tests.phones.features.TestFeatures.createPhoneEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import es.microservices.tests.phones.mappers.Mapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PhoneEntity2PhoneDtoMapperTest {

  private static Mapper<PhoneEntity, PhoneDto> mapper;

  @BeforeAll
  public static void beforeAll() {
    mapper = new PhoneEntity2PhoneDtoMapper();
  }

  @Test
  void phoneEntity_input_isNull() {
    final PhoneEntity input = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(input);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void phoneEntity_input_mapOk() {
    final String phoneId = "phoneId";
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final PhoneEntity input =
        createPhoneEntity(phoneId, phoneName, phoneDescription, phonePrice, phoneImageURL);

    final PhoneDto entity = mapper.map(input);

    assertNotNull(entity);
    assertEquals(input.getId(), entity.getPhoneId());
    assertEquals(input.getName(), entity.getName());
    assertEquals(input.getDescription(), entity.getDescription());
    assertEquals(input.getPrice(), entity.getPrice());
    assertEquals(input.getImageURL(), entity.getImageURL());
  }



  @Test
  void flux_phoneEntity_input_isNull() {
    final PhoneEntity input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Flux.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void flux_phoneEntity_flux_isNull() {
    Flux<PhoneEntity> flux = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(flux);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void flux_phoneEntity_input_mapOk() {
    final String phoneId = "phoneId";
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";

    final PhoneEntity input =
        createPhoneEntity(phoneId, phoneName, phoneDescription, phonePrice, phoneImageURL);

    final Flux<PhoneDto> entityFlux = mapper.map(Flux.just(input));

    StepVerifier.create(entityFlux).assertNext(entity -> {
      assertNotNull(entity);
      assertEquals(input.getId(), entity.getPhoneId());
      assertEquals(input.getName(), entity.getName());
      assertEquals(input.getDescription(), entity.getDescription());
      assertEquals(input.getPrice(), entity.getPrice());
      assertEquals(input.getImageURL(), entity.getImageURL());
    }).expectComplete().verify();
  }


  @Test
  void mono_phoneEntity_input_isNull() {
    final PhoneEntity input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Mono.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void mono_phoneEntity_mono_isNull() {
    Mono<PhoneEntity> mono = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(mono);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void mono_phoneEntity_input_mapOk() {
    final String phoneId = "phoneId";
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final PhoneEntity input =
        createPhoneEntity(phoneId, phoneName, phoneDescription, phonePrice, phoneImageURL);

    final Mono<PhoneDto> entityFlux = mapper.map(Mono.just(input));

    StepVerifier.create(entityFlux).assertNext(entity -> {
      assertNotNull(entity);
      assertEquals(input.getId(), entity.getPhoneId());
      assertEquals(input.getName(), entity.getName());
      assertEquals(input.getDescription(), entity.getDescription());
      assertEquals(input.getPrice(), entity.getPrice());
      assertEquals(input.getImageURL(), entity.getImageURL());
    }).expectComplete().verify();
  }


}
