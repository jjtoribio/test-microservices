package es.microservices.tests.phones.controllers.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import es.microservices.tests.phones.configurations.ErrorManagementConfig;
import es.microservices.tests.phones.configurations.GlobalConfig;
import es.microservices.tests.phones.controllers.handler.SpecificControllerAdvice;
import es.microservices.tests.phones.dtos.ErrorResponse;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneCatalogDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import es.microservices.tests.phones.features.TestFeatures;
import es.microservices.tests.phones.repositories.PhoneRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PhoneControllerImplTest.InnerConfiguration.class})
@WebFluxTest(controllers = {PhoneControllerImpl.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient(timeout = "300000")
public class PhoneControllerImplTest {

  private static final String URL = "/phones";

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private PhoneRepository repository;

  @BeforeAll
  public static void beforeAll() {
    Locale.setDefault(Locale.US);
  }
  
  @BeforeEach
  public void beforeEach() {
    // BlockHound.install();
    Mockito.clearInvocations(this.repository);
    
  }

  @Test
  void testGetPhoneCatalog_findAll_returnsOk_withOut_queryParameters() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 1l;

    final int returnedPhonesExpected = 1;

    final String phoneId = "1";
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "phoneImageURL";

    final PhoneEntity entity = TestFeatures.createPhoneEntity(phoneId, phoneName, phoneDescription,
        phonePrice, phoneImageURL);
    final PhoneDto expectedPhoneDto = TestFeatures.createPhoneDto(phoneId, phoneName,
        phoneDescription, phonePrice, phoneImageURL);

    doReturn(Flux.just(entity)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(PhoneCatalogDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getPhones(), notNullValue())
        .value(consume -> consume.getPhones().size(), equalTo(returnedPhonesExpected))
        .value(consume -> consume.getPhones().get(0), equalTo(expectedPhoneDto))
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetPhoneCatalog_findAll_returns_400_when_page_parameter_is_not_a_number() {
    final String page = "a";
    final int pageSize = 10;

    final String errorMessage = "Type mismatch.";

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isBadRequest()
          .expectBody(ErrorResponse.class)
            .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
            .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
            .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;
    
    // @formatter:on

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();
  }

  @Test
  void testGetPhoneCatalog_findAll_returns_400_when_pageSize_parameter_is_not_a_number() {
    final int page = 1;
    final String pageSize = "a";

    final String errorMessage = "Type mismatch.";

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isBadRequest()
          .expectBody(ErrorResponse.class)
            .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
            .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
            .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;
    
    // @formatter:on
    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();
  }

  @Test
  void testGetPhoneCatalog_findAll_returnsError() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 0l;

    final String errorMessage = "An error occurred retrieving the data";

    doReturn(Flux.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetPhoneCatalog_count_returnsError() {
    final int page = 1;
    final int pageSize = 10;
    final String phoneId = "1";
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "phoneImageURL";

    final String errorMessage = "An error occurred counting the data";

    final PhoneEntity entity = TestFeatures.createPhoneEntity(phoneId, phoneName, phoneDescription,
        phonePrice, phoneImageURL);

    doReturn(Flux.just(entity)).when(this.repository).findAll();
    doReturn(Mono.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetPhoneCatalog_returnsEmptyPhoneList() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 0l;

    final int returnedPhonesExpected = 0;

    doReturn(Flux.empty()).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(PhoneCatalogDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getPhones(), notNullValue())
        .value(consume -> consume.getPhones().size(), equalTo(returnedPhonesExpected));        
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetPhoneCatalog_returnsOnePhoneList() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 1l;

    final int returnedPhonesExpected = 1;

    final String phoneId = "1";
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "phoneImageURL";

    final PhoneEntity entity = TestFeatures.createPhoneEntity(phoneId, phoneName, phoneDescription,
        phonePrice, phoneImageURL);
    final PhoneDto expectedPhoneDto = TestFeatures.createPhoneDto(phoneId, phoneName,
        phoneDescription, phonePrice, phoneImageURL);

    doReturn(Flux.just(entity)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(PhoneCatalogDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getPhones(), notNullValue())
        .value(consume -> consume.getPhones().size(), equalTo(returnedPhonesExpected))
        .value(consume -> consume.getPhones().get(0), equalTo(expectedPhoneDto))        
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetPhoneCatalog_returnsTwoPhonesList() {
    final int page = 1;
    final int pageSize = 10;
    final Long totalCount = 2l;

    final int returnedPhonesExpected = 2;

    final String phoneId1 = "1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final Double phonePrice1 = 1.0;
    final String phoneImageURL1 = "phoneImageURL1";

    final String phoneId2 = "2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final Double phonePrice2 = 2.0;
    final String phoneImageURL2 = "phoneImageURL2";

    final PhoneEntity entity1 = TestFeatures.createPhoneEntity(phoneId1, phoneName1,
        phoneDescription1, phonePrice1, phoneImageURL1);
    final PhoneDto expectedPhoneDto1 = TestFeatures.createPhoneDto(phoneId1, phoneName1,
        phoneDescription1, phonePrice1, phoneImageURL1);

    final PhoneEntity entity2 = TestFeatures.createPhoneEntity(phoneId2, phoneName2,
        phoneDescription2, phonePrice2, phoneImageURL2);
    final PhoneDto expectedPhoneDto2 = TestFeatures.createPhoneDto(phoneId2, phoneName2,
        phoneDescription2, phonePrice2, phoneImageURL2);

    doReturn(Flux.just(entity1, entity2)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(PhoneCatalogDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getPhones(), notNullValue())
        .value(consume -> consume.getPhones().size(), equalTo(returnedPhonesExpected))
        .value(consume -> consume.getPhones().get(0), equalTo(expectedPhoneDto1))
        .value(consume -> consume.getPhones().get(1), equalTo(expectedPhoneDto2))        
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetPhoneCatalog_returnsTwoPhonesList_pageSize_1_page_1() {
    final int page = 1;
    final int pageSize = 1;
    final Long totalCount = 2l;

    final int returnedPhonesExpected = 1;

    final String phoneId1 = "1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final Double phonePrice1 = 1.0;
    final String phoneImageURL1 = "phoneImageURL1";

    final String phoneId2 = "2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final Double phonePrice2 = 2.0;
    final String phoneImageURL2 = "phoneImageURL2";

    final PhoneEntity entity1 = TestFeatures.createPhoneEntity(phoneId1, phoneName1,
        phoneDescription1, phonePrice1, phoneImageURL1);
    final PhoneEntity entity2 = TestFeatures.createPhoneEntity(phoneId2, phoneName2,
        phoneDescription2, phonePrice2, phoneImageURL2);

    final PhoneDto expectedPhoneDto1 = TestFeatures.createPhoneDto(phoneId1, phoneName1,
        phoneDescription1, phonePrice1, phoneImageURL1);

    doReturn(Flux.just(entity1, entity2)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(PhoneCatalogDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getPhones(), notNullValue())
        .value(consume -> consume.getPhones().size(), equalTo(returnedPhonesExpected))
        .value(consume -> consume.getPhones().get(0), equalTo(expectedPhoneDto1))
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }

  @Test
  void testGetPhoneCatalog_returnsTwoPhonesList_pageSize_1_page_2() {
    final int page = 2;
    final int pageSize = 1;
    final Long totalCount = 2l;

    final int returnedPhonesExpected = 1;

    final String phoneId1 = "1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final Double phonePrice1 = 1.0;
    final String phoneImageURL1 = "phoneImageURL1";

    final String phoneId2 = "2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final Double phonePrice2 = 2.0;
    final String phoneImageURL2 = "phoneImageURL2";

    final PhoneEntity entity1 = TestFeatures.createPhoneEntity(phoneId1, phoneName1,
        phoneDescription1, phonePrice1, phoneImageURL1);
    final PhoneEntity entity2 = TestFeatures.createPhoneEntity(phoneId2, phoneName2,
        phoneDescription2, phonePrice2, phoneImageURL2);

    final PhoneDto expectedPhoneDto2 = TestFeatures.createPhoneDto(phoneId2, phoneName2,
        phoneDescription2, phonePrice2, phoneImageURL2);

    doReturn(Flux.just(entity1, entity2)).when(this.repository).findAll();
    doReturn(Mono.just(totalCount)).when(this.repository).count();

    verify(this.repository, never()).findAll();
    verify(this.repository, never()).count();

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(PhoneCatalogDto.class)
        .value(consume -> consume.getPage(), equalTo(page))
        .value(consume -> consume.getPageSize(), equalTo(pageSize))
        .value(consume -> consume.getTotalCount(), equalTo(totalCount))
        .value(consume -> consume.getPhones(), notNullValue())
        .value(consume -> consume.getPhones().size(), equalTo(returnedPhonesExpected))
        .value(consume -> consume.getPhones().get(0), equalTo(expectedPhoneDto2))
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findAll();
    verify(this.repository, times(1)).count();
  }



  @Test
  void testGetPhoneById_phoneId_found() {
    final String phoneId = "1";
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "phoneImageURL";

    final PhoneEntity entity = TestFeatures.createPhoneEntity(phoneId, phoneName, phoneDescription,
        phonePrice, phoneImageURL);

    doReturn(Mono.just(entity)).when(this.repository).findById(anyString());

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL.concat("/{phoneId}"))
            .build(phoneId))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isOk()
        .expectBody(PhoneDto.class)
        .value(consume -> consume.getPhoneId(), equalTo(phoneId))
        .value(consume -> consume.getName(), equalTo(phoneName))
        .value(consume -> consume.getDescription(), equalTo(phoneDescription))
        .value(consume -> consume.getPrice(), equalTo(phonePrice))
        .value(consume -> consume.getImageURL(), equalTo(phoneImageURL))
        ;
    
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void testGetPhoneById_phoneId_notFound() {
    final String phoneId = "1";

    final String errorMessage = "Resource not found!";

    doReturn(Mono.empty()).when(this.repository).findById(anyString());

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL.concat("/{phoneId}"))
            .build(phoneId))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .isNotFound()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.NOT_FOUND.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;    
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }

  @Test
  void testGetPhoneById_findById_returns_error() {
    final String phoneId = "1";

    final String errorMessage = "An error ocurrs finding the phone";

    doReturn(Mono.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).findById(anyString());

    verify(this.repository, never()).findById(anyString());

    // @formatter:off
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path(URL.concat("/{phoneId}"))
            .build(phoneId))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );
        ;    
    // @formatter:on

    verify(this.repository, times(1)).findById(anyString());
  }


  @Test
  void testPostAddPhoneToCatalog_save_method_returns_error() {
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        TestFeatures.createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "An error ocurrs saving the new phone";

    doReturn(Mono.error(() -> new DataRetrievalFailureException(errorMessage)))
        .when(this.repository).save(any(PhoneEntity.class));

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, times(1)).save(any(PhoneEntity.class));
  }

  @Test
  void testPostAddPhoneToCatalog_phoneName_isNull() {
    final String phoneName = null;
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "name: must not be blank";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }

  @Test
  void testPostAddPhoneToCatalog_phoneName_isEmpty() {
    final String phoneName = " ";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "name: must not be blank";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }
  
  @Test
  void testPostAddPhoneToCatalog_phoneDescription_isNull() {
    final String phoneName = "phoneName";
    final String phoneDescription = null;
    final Double phonePrice = 1.0;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "description: must not be blank";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }

  @Test
  void testPostAddPhoneToCatalog_phoneDescription_isEmpty() {
    final String phoneName = "phoneName";
    final String phoneDescription = "  ";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "description: must not be blank";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }
  
  @Test
  void testPostAddPhoneToCatalog_phonePrice_isNull() {
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = null;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "price: must not be null";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }
  
  @Test
  void testPostAddPhoneToCatalog_phonePrice_lessThan0() {
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = -0.1;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "price: the price cannot be less than 0";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }
  
  @Test
  void testPostAddPhoneToCatalog_phoneImageURL_isNull() {
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = null;

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "imageURL: must not be blank";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }

  @Test
  void testPostAddPhoneToCatalog_phoneImageURL_isEmpty() {
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = " ";

    final NewPhoneDto newPhone =
        new NewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);

    final String errorMessage = "imageURL: must not be blank";

    verify(this.repository, never()).save(any(PhoneEntity.class));

 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(consume -> consume.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()))
        .value(consume -> consume.getErrorMessage(), equalTo(errorMessage))
        .value(consume -> consume.getOperationId(), notNullValue(String.class) );       
        ;    
    // @formatter:on
    verify(this.repository, never()).save(any(PhoneEntity.class));
  }
  

  @Test
  void testPostAddPhoneToCatalog_createOk() {
    final String phoneId = "1";
    final String phoneName = "phoneName";
    final String phoneDescription = "phoneDescription";
    final Double phonePrice = 1.0;
    final String phoneImageURL = "http://phoneImageURL/image.png";

    final NewPhoneDto newPhone =
        TestFeatures.createNewPhoneDto(phoneName, phoneDescription, phonePrice, phoneImageURL);
    final PhoneEntity entity = TestFeatures.createPhoneEntity(phoneId, phoneName, phoneDescription,
        phonePrice, phoneImageURL);

    doReturn(Mono.just(entity)).when(this.repository).save(any(PhoneEntity.class));

    verify(this.repository, never()).save(any(PhoneEntity.class));
 // @formatter:off
    webTestClient.post()
        .uri(uriBuilder -> uriBuilder.path(URL)
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(newPhone)
        .exchange()
          .expectStatus()
            .isCreated()
        .expectBody(PhoneDto.class)
        .value(consume -> consume.getPhoneId(), notNullValue())
        .value(consume -> consume.getName(), equalTo(phoneName))
        .value(consume -> consume.getDescription(), equalTo(phoneDescription))
        .value(consume -> consume.getPrice(), equalTo(phonePrice))
        .value(consume -> consume.getImageURL(), equalTo(phoneImageURL))        
        ;    
    // @formatter:on

    verify(this.repository, times(1)).save(any(PhoneEntity.class));
  }



  @TestConfiguration
  @EnableAutoConfiguration
  @Import({PhoneControllerImpl.class, GlobalConfig.class, ErrorManagementConfig.class,
      SpecificControllerAdvice.class})
  static class InnerConfiguration {

    @Bean
    public PhoneRepository repository() {
      return mock(PhoneRepository.class);
    }

    @Bean
    public Tracer tracer() {
      final List<Span> spans = new ArrayList<>();
      Reporter<Span> reporter = spans::add;
      CurrentTraceContext currentTraceContext = CurrentTraceContext.Default.inheritable();
      currentTraceContext
          .newScope(TraceContext.newBuilder().traceIdHigh(-1L).traceId(1L).spanId(1L).build());
      Tracing tracing = Tracing.newBuilder().currentTraceContext(currentTraceContext)
          .localServiceName("localServiceName").spanReporter(reporter).sampler(Sampler.NEVER_SAMPLE)
          .build();
      return tracing.tracer();
    }

  }
}
