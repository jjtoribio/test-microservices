package es.microservices.tests.orders.clients.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.google.common.net.HttpHeaders;
import es.microservices.tests.orders.clients.PhoneClient;
import es.microservices.tests.orders.configurations.properties.PhoneClientProperties;
import es.microservices.tests.orders.dtos.phones.DaasPhoneCatalogDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;



class PhoneClientImplTest {

  private static final Integer DEFAULT_PAGE_SIZE = Integer.valueOf(10);
  private static final Integer DEFAULT_PAGE_NUMBER = Integer.valueOf(1);

  private static MockWebServer mockBackEnd;

  private PhoneClient client;

  private static final String URL_CALLED = "/phones?page=%d&pageSize=%d";
  private RestTemplate restTemplate;

  private PhoneClientProperties clientConfig;

  @BeforeAll
  public static void setUp() throws IOException {
    mockBackEnd = new MockWebServer();
    mockBackEnd.start();
    Locale.setDefault(Locale.US);
  }

  @AfterAll
  public static void tearDown() throws IOException {
    mockBackEnd.shutdown();
  }

  @BeforeEach
  public void before() {
    final String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    final String endpointUrl = "/phones";
    this.restTemplate = new RestTemplate();
    this.clientConfig = new PhoneClientProperties();
    this.clientConfig.setBaseUrl(baseUrl);
    this.clientConfig.setEndpointUrl(endpointUrl);
    this.client = new PhoneClientImpl(restTemplate, clientConfig);
  }

  @Test
  void webClientBuilderIsNull_must_throw_IllegalArgumentException() {
    final RestTemplate restTemplate = null;
    final PhoneClientProperties clientConfig = this.clientConfig;

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> new PhoneClientImpl(restTemplate, clientConfig));

    final String expectedMessage = "'restTemplate' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void clientConfigIsNull_must_throw_IllegalArgumentException() {
    final RestTemplate restTemplate = this.restTemplate;
    final PhoneClientProperties clientConfig = null;

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> new PhoneClientImpl(restTemplate, clientConfig));

    final String expectedMessage = "'phoneProperties' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void call_page_isNull_must_throw_IllegalArgumentException() {
    final Integer page = null;
    final Integer pageSize = DEFAULT_PAGE_SIZE;

    Exception exception =
        assertThrows(IllegalArgumentException.class, executePhoneClient(page, pageSize));

    final String expectedMessage = "'page' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void call_page_less_than_one_must_throw_IllegalArgumentException() {
    final Integer page = Integer.valueOf(0);
    final Integer pageSize = DEFAULT_PAGE_SIZE;

    Exception exception =
        assertThrows(IllegalArgumentException.class, executePhoneClient(page, pageSize));

    final String expectedMessage = "'page' must be greater than zero";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

  }

  @Test
  void call_pageSize_isNull_must_throw_IllegalArgumentException() {
    final Integer page = DEFAULT_PAGE_NUMBER;
    final Integer pageSize = null;

    Exception exception =
        assertThrows(IllegalArgumentException.class, executePhoneClient(page, pageSize));

    final String expectedMessage = "'pageSize' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void call_pageSize_less_than_one_must_throw_IllegalArgumentException() {
    final Integer page = DEFAULT_PAGE_NUMBER;
    final Integer pageSize = Integer.valueOf(0);

    Exception exception =
        assertThrows(IllegalArgumentException.class, executePhoneClient(page, pageSize));

    final String expectedMessage = "'pageSize' must be greater than zero";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }



  @Test
  void call_return_4xx_status_code() throws InterruptedException {
    final Integer page = DEFAULT_PAGE_NUMBER;
    final Integer pageSize = DEFAULT_PAGE_NUMBER;

    final int statusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();

    mockBackEnd.enqueue(new MockResponse().setResponseCode(statusCode));

    Exception exception = assertThrows(RestClientException.class, executePhoneClient(page, pageSize));

    final String expectedMessage = "422 Client Error: [no body]";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertions(page, pageSize, recordedRequest);
  }

  @Test
  void call_return_5xx_status_code() throws InterruptedException {
    final Integer page = DEFAULT_PAGE_NUMBER;
    final Integer pageSize = DEFAULT_PAGE_NUMBER;
    final int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

    mockBackEnd.enqueue(new MockResponse().setResponseCode(statusCode));

    Exception exception = assertThrows(RestClientException.class, executePhoneClient(page, pageSize));

    final String expectedMessage = "500 Server Error: [no body]";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertions(page, pageSize, recordedRequest);
  }

  @Test
  void call_return_200_status_code() throws InterruptedException {
    final Integer page = DEFAULT_PAGE_NUMBER;
    final Integer pageSize = DEFAULT_PAGE_NUMBER;
    final Long totalCount = 1l;
    final String phoneId = "phoneId";
    final String phoneName = "phoneName";
    final Double phonePrice = 100.99;
    final String phoneDescription = "phoneDescription";
    final String phoneImageURL = "phoneImageURL";

    final int statusCode = HttpStatus.OK.value();

    final DaasPhoneDto phone =
        createPhoneDto(phoneId, phoneName, phonePrice, phoneDescription, phoneImageURL);
    final DaasPhoneCatalogDto expectedResponse =
        createPhoneCatalogDtoObject(Arrays.asList(phone), page, pageSize, totalCount);
    final String body = createBodyResponse(phoneId, phoneName, phoneDescription, phonePrice,
        phoneImageURL, page, pageSize, totalCount);

    mockBackEnd.enqueue(new MockResponse().setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).setBody(body)
        .setResponseCode(statusCode));

    final DaasPhoneCatalogDto response = this.client.getPhoneData(page, pageSize);
    assertNotNull(response);
    assertEquals(expectedResponse, response);

    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertions(page, pageSize, recordedRequest);

  }

  private DaasPhoneDto createPhoneDto(final String phoneId, final String phoneName,
      final Double phonePrice, final String phoneDescription, final String phoneImageURL) {
    // @formatter:off
    return DaasPhoneDto.builder()
        .phoneId(phoneId)
        .name(phoneName)
        .description(phoneDescription)
        .price(phonePrice)
        .imageURL(phoneImageURL)
        .build();
    // @formatter:on
  }


  private Executable executePhoneClient(final Integer page, final Integer pageSize) {
    return new Executable() {
      @Override
      public void execute() throws Throwable {
        PhoneClientImplTest.this.client.getPhoneData(page, pageSize);
      }
    };
  }

  private static DaasPhoneCatalogDto createPhoneCatalogDtoObject(
      final Collection<DaasPhoneDto> phones, final Integer page, final Integer pageSize,
      final Long totalCount) {
    // @formatter:off
    return DaasPhoneCatalogDto.builder()
        .phones(phones)
        .page(page)
        .pageSize(pageSize)
        .totalCount(totalCount)
        .build();
  // @formatter:on
  }

  private static String createBodyResponse(final String phoneId, final String phoneName,
      final String phoneDescription, final Double phonePrice, final String phoneImageURL,
      final Integer page, final Integer pageSize, final Long totalCount) {
    // @formatter:off
    return String.format(
        "{\n" + 
        "  \"phones\": [\n" + 
        "    {\n" + 
        "      \"phoneId\": \"%s\",\n" + 
        "      \"name\": \"%s\",\n" + 
        "      \"description\": \"%s\",\n" + 
        "      \"price\": %.2f,\n" + 
        "      \"imageURL\": \"%s\"\n" + 
        "    }\n" + 
        "  ],\n" + 
        "  \"pageSize\": %d,\n" + 
        "  \"page\": %d,\n" + 
        "  \"total-count\": %d\n" + 
        "}", 
        phoneId, phoneName, phoneDescription, phonePrice, phoneImageURL, page, pageSize, totalCount);
    // @formatter:on
  }

  private static void assertions(final Integer page, final Integer pageSize,
      final RecordedRequest recordedRequest) {
    Assertions.assertEquals("GET", recordedRequest.getMethod());
    Assertions.assertEquals(String.format(URL_CALLED, page, pageSize), recordedRequest.getPath());
  }

}
