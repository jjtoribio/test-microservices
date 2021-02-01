package es.microservices.tests.phones.mappers.impl;

import static es.microservices.tests.phones.features.TestFeatures.createNewPhoneDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import es.microservices.tests.phones.mappers.Mapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class NewPhoneDto2PhoneEntityMapperTest {

  private static Mapper<NewPhoneDto, PhoneEntity> mapper;

  @BeforeAll
  public static void beforeAll() {
    mapper = new NewPhoneDto2PhoneEntityMapper();
  }

  @Test
  void newPhoneDto_input_isNull() {
    final NewPhoneDto input = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(input);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void newPhoneDto_input_mapOk() {
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final NewPhoneDto input =
        createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final PhoneEntity entity = mapper.map(input);

    assertNotNull(entity);
    assertTrue(StringUtils.isNotBlank(entity.getId()));
    assertEquals(input.getName(), entity.getName());
    assertEquals(input.getDescription(), entity.getDescription());
    assertEquals(input.getPrice(), entity.getPrice());
    assertEquals(input.getImageURL(), entity.getImageURL());
  }



  @Test
  void flux_newPhoneDto_input_isNull() {
    final NewPhoneDto input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Flux.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }
  
  @Test
  void flux_newPhoneDto_flux_isNull() {
    Flux<NewPhoneDto> flux = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(flux);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void flux_newPhoneDto_input_mapOk() {
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final NewPhoneDto input =
        createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final Flux<PhoneEntity> entityFlux = mapper.map(Flux.just(input));

    StepVerifier.create(entityFlux).assertNext(entity -> {
      assertNotNull(entity);
      assertTrue(StringUtils.isNotBlank(entity.getId()));
      assertEquals(input.getName(), entity.getName());
      assertEquals(input.getDescription(), entity.getDescription());
      assertEquals(input.getPrice(), entity.getPrice());
      assertEquals(input.getImageURL(), entity.getImageURL());
    }).expectComplete().verify();
  }
  

  @Test
  void mono_newPhoneDto_input_isNull() {
    final NewPhoneDto input = null;

    Exception exception = assertThrows(NullPointerException.class, () -> {
      mapper.map(Mono.just(input));
    });

    final String expectedMessage = "value";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }
  
  @Test
  void mono_newPhoneDto_mono_isNull() {
    Mono<NewPhoneDto> mono = null;
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapper.map(mono);
    });

    final String expectedMessage = "'input' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void mono_newPhoneDto_input_mapOk() {
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final NewPhoneDto input =
        createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final Mono<PhoneEntity> entityFlux = mapper.map(Mono.just(input));

    StepVerifier.create(entityFlux).assertNext(entity -> {
      assertNotNull(entity);
      assertTrue(StringUtils.isNotBlank(entity.getId()));
      assertEquals(input.getName(), entity.getName());
      assertEquals(input.getDescription(), entity.getDescription());
      assertEquals(input.getPrice(), entity.getPrice());
      assertEquals(input.getImageURL(), entity.getImageURL());
    }).expectComplete().verify();
  }

}
