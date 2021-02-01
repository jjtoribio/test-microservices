package es.microservices.tests.phones.services.impl;

import static es.microservices.tests.phones.features.TestFeatures.createNewPhoneDto;
import static es.microservices.tests.phones.features.TestFeatures.createPhoneEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import es.microservices.tests.phones.mappers.Mapper;
import es.microservices.tests.phones.mappers.impl.NewPhoneDto2PhoneEntityMapper;
import es.microservices.tests.phones.mappers.impl.PhoneEntity2PhoneDtoMapper;
import es.microservices.tests.phones.repositories.PhoneRepository;
import es.microservices.tests.phones.services.PhoneService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PhoneServiceImplTest {

  private PhoneService service;
  private PhoneRepository repository;

  private Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper;
  private Mapper<PhoneEntity, PhoneDto> entity2DtoMapper;


  @BeforeEach
  public void before() {
    // BlockHound.install();
    this.dto2EntityMapper = new NewPhoneDto2PhoneEntityMapper();
    this.entity2DtoMapper = new PhoneEntity2PhoneDtoMapper();

    this.repository = mock(PhoneRepository.class);
    this.service =
        new PhoneServiceImpl(this.repository, this.dto2EntityMapper, this.entity2DtoMapper);
  }

  @Test
  void repository_isNull() {
    final PhoneRepository repository = null;
    final Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper = this.dto2EntityMapper;
    final Mapper<PhoneEntity, PhoneDto> entity2DtoMapper = this.entity2DtoMapper;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new PhoneServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
    });

    final String expectedMessage = "'repository' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void dto2EntityMapper_isNull() {
    final PhoneRepository repository = this.repository;
    final Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper = null;
    final Mapper<PhoneEntity, PhoneDto> entity2DtoMapper = this.entity2DtoMapper;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new PhoneServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
    });

    final String expectedMessage = "'dto2EntityMapper' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void entity2DtoMapper_isNull() {
    final PhoneRepository repository = this.repository;
    final Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper = this.dto2EntityMapper;
    final Mapper<PhoneEntity, PhoneDto> entity2DtoMapper = null;

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new PhoneServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
    });

    final String expectedMessage = "'entity2DtoMapper' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void findAllPhones_returnEmptyFlux() {
    doReturn(Flux.empty()).when(this.repository).findAll();

    Flux<PhoneDto> fluxPhoneCatalog = this.service.findAllPhones();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxPhoneCatalog)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllPhones_throwsError() {
    doThrow(new DataRetrievalFailureException("error")).when(this.repository).findAll();

    Flux<PhoneDto> fluxPhoneCatalog = this.service.findAllPhones();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxPhoneCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllPhones_returnsFluxError() {
    doReturn(Flux.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .findAll();

    Flux<PhoneDto> fluxPhoneCatalog = this.service.findAllPhones();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxPhoneCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllPhones_returnOneElementFlux() {
    final String phone1Id = "1";
    final String phone1Name = "phoneName1";
    final String phone1Description = "phone1Description";
    final Double phone1Price = 305.33;
    final String phone1ImageURL = "phone1ImageURL";
    final PhoneEntity phone1 =
        createPhoneEntity(phone1Id, phone1Name, phone1Description, phone1Price, phone1ImageURL);
    doReturn(Flux.just(phone1)).when(this.repository).findAll();

    Flux<PhoneDto> fluxPhoneCatalog = this.service.findAllPhones();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxPhoneCatalog)
      .assertNext(phoneDto -> {
        assertEquals(phone1Id, phoneDto.getPhoneId());
        assertEquals(phone1Name, phoneDto.getName());
        assertEquals(phone1Description, phoneDto.getDescription());
        assertEquals(phone1Price, phoneDto.getPrice());
        assertEquals(phone1ImageURL, phoneDto.getImageURL());
      })
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }

  @Test
  void findAllPhones_returnTwoElementsFlux() {
    final String phone1Id = "1";
    final String phone1Name = "phoneName1";
    final String phone1Description = "phone1Description";
    final Double phone1Price = 305.33;
    final String phone1ImageURL = "phone1ImageURL";
    final String phone2Id = "2";
    final String phone2Name = "phoneName2";
    final String phone2Description = "phone2Description";
    final Double phone2Price = 900.95;
    final String phone2ImageURL = "phone2ImageURL";

    final PhoneEntity phone1 =
        createPhoneEntity(phone1Id, phone1Name, phone1Description, phone1Price, phone1ImageURL);
    final PhoneEntity phone2 =
        createPhoneEntity(phone2Id, phone2Name, phone2Description, phone2Price, phone2ImageURL);

    doReturn(Flux.just(phone1, phone2)).when(this.repository).findAll();

    Flux<PhoneDto> fluxPhoneCatalog = this.service.findAllPhones();

    verify(this.repository, never()).findAll();

    // @formatter:off
    StepVerifier.create(fluxPhoneCatalog)
      .assertNext(phoneDto -> {
        assertEquals(phone1Id, phoneDto.getPhoneId());
        assertEquals(phone1Name, phoneDto.getName());
        assertEquals(phone1Description, phoneDto.getDescription());
        assertEquals(phone1Price, phoneDto.getPrice());
        assertEquals(phone1ImageURL, phoneDto.getImageURL());
      })
      .assertNext(phoneDto -> {
        assertEquals(phone2Id, phoneDto.getPhoneId());
        assertEquals(phone2Name, phoneDto.getName());
        assertEquals(phone2Description, phoneDto.getDescription());
        assertEquals(phone2Price, phoneDto.getPrice());
        assertEquals(phone2ImageURL, phoneDto.getImageURL());
      })
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findAll();
  }



  @Test
  void countAllPhones_returnEmptyMono() {
    doReturn(Mono.empty()).when(this.repository).count();

    final Mono<Long> monoPhoneCatalogCount = this.service.countAllPhones();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoPhoneCatalogCount)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }

  @Test
  void countAllPhones_throwsError() {
    doThrow(new DataRetrievalFailureException("error")).when(this.repository).count();

    final Mono<Long> monoPhoneCatalogCount = this.service.countAllPhones();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoPhoneCatalogCount)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }

  @Test
  void countAllPhones_returnsFluxError() {
    doReturn(Mono.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .count();

    final Mono<Long> monoPhoneCatalogCount = this.service.countAllPhones();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoPhoneCatalogCount)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }

  @Test
  void countAllPhones_returnCounter() {
    doReturn(Mono.just(1l)).when(this.repository).count();

    final Mono<Long> monoPhoneCatalogCount = this.service.countAllPhones();

    verify(this.repository, never()).count();

    // @formatter:off
    StepVerifier.create(monoPhoneCatalogCount)
      .expectNext(1l)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).count();
  }


  @Test
  void findById_returnEmptyMono() {
    final String phoneId = "1";
    doReturn(Mono.empty()).when(this.repository).findById(anyString());

    final Mono<PhoneDto> monoPhoneCatalog = this.service.findById(phoneId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoPhoneCatalog)
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void findById_throwsError() {
    final String phoneId = "1";
    doThrow(new DataRetrievalFailureException("error")).when(this.repository).findById(anyString());

    final Mono<PhoneDto> monoPhoneCatalog = this.service.findById(phoneId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoPhoneCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void findById_returnsMonoError() {
    final String phoneId = "1";
    doReturn(Mono.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .findById(anyString());

    final Mono<PhoneDto> monoPhoneCatalog = this.service.findById(phoneId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoPhoneCatalog)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void findById_returnTheElement() {
    final String phoneId = "1";
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final PhoneEntity phone =
        createPhoneEntity(phoneId, phoneName, phoneDescription, phonePrice, phoneImageURL);
    doReturn(Mono.just(phone)).when(this.repository).findById(anyString());

    Mono<PhoneDto> monoPhoneCatalog = this.service.findById(phoneId);

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    StepVerifier.create(monoPhoneCatalog)
      .assertNext(phoneDto -> {
        assertEquals(phoneId, phoneDto.getPhoneId());
        assertEquals(phoneName, phoneDto.getName());
        assertEquals(phoneDescription, phoneDto.getDescription());
        assertEquals(phonePrice, phoneDto.getPrice());
        assertEquals(phoneImageURL, phoneDto.getImageURL());
      })
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }


  @Test
  void createPhone_throwsError() {
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final NewPhoneDto newPhone =
        createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    doThrow(new DataRetrievalFailureException("error")).when(this.repository)
        .save(any(PhoneEntity.class));

    final Mono<PhoneDto> monoPhone = this.service.createPhone(newPhone);

    verify(this.repository, never()).save(any(PhoneEntity.class));

    // @formatter:off
    StepVerifier.create(monoPhone)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).save(any(PhoneEntity.class));
  }

  @Test
  void createPhone_returnsMonoError() {
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final NewPhoneDto newPhone =
        createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    doReturn(Mono.error(() -> new DataRetrievalFailureException("error"))).when(this.repository)
        .save(any(PhoneEntity.class));

    final Mono<PhoneDto> monoPhone = this.service.createPhone(newPhone);

    verify(this.repository, never()).save(any(PhoneEntity.class));

    // @formatter:off
    StepVerifier.create(monoPhone)
      .expectError(DataAccessException.class)
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).save(any(PhoneEntity.class));
  }

  @Test
  void createPhone_createOk() {
    final String phoneId = UUID.randomUUID().toString();
    final String phoneName = "phoneName1";
    final String phoneDescription = "phone1Description";
    final Double phonePrice = 305.33;
    final String phoneImageURL = "phone1ImageURL";
    final NewPhoneDto newPhone =
        createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);
    final PhoneEntity phone =
        createPhoneEntity(phoneId, phoneName, phoneDescription, phonePrice, phoneImageURL);
    doReturn(Mono.just(phone)).when(this.repository).save(any(PhoneEntity.class));

    Mono<PhoneDto> monoPhone = this.service.createPhone(newPhone);

    verify(this.repository, never()).save(any(PhoneEntity.class));

    // @formatter:off
    StepVerifier.create(monoPhone)
      .assertNext(phoneDto -> {
        assertTrue(StringUtils.isNotBlank(phoneDto.getPhoneId()));
        assertEquals(phoneName, phoneDto.getName());
        assertEquals(phoneDescription, phoneDto.getDescription());
        assertEquals(phonePrice, phoneDto.getPrice());
        assertEquals(phoneImageURL, phoneDto.getImageURL());
      })
      .expectComplete()
      .verify();
    // @formatter:on

    verify(this.repository, times(1)).save(any(PhoneEntity.class));
  }

}
