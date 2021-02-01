package es.microservices.tests.orders.clients.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
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
import es.microservices.tests.orders.clients.OrderClient;
import es.microservices.tests.orders.configurations.properties.OrderClientProperties;
import es.microservices.tests.orders.dtos.orders.DaasCustomerDto;
import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasPhoneOrderDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class OrderClientImplTest {

  private static MockWebServer mockBackEnd;

  private OrderClient client;

  private static final String URL_CALLED = "/orders";
  private RestTemplate restTemplate;

  private OrderClientProperties clientConfig;

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
    final String endpointUrl = "/orders";
    this.restTemplate = new RestTemplate();
    this.clientConfig = new OrderClientProperties();
    this.clientConfig.setBaseUrl(baseUrl);
    this.clientConfig.setEndpointUrl(endpointUrl);
    this.client = new OrderClientImpl(restTemplate, clientConfig);
  }

  @Test
  void webClientBuilderIsNull_must_throw_IllegalArgumentException() {
    final RestTemplate restTemplate = null;
    final OrderClientProperties clientConfig = this.clientConfig;

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> new OrderClientImpl(restTemplate, clientConfig));

    final String expectedMessage = "'restTemplate' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void clientConfigIsNull_must_throw_IllegalArgumentException() {
    final RestTemplate restTemplate = this.restTemplate;
    final OrderClientProperties clientConfig = null;

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> new OrderClientImpl(restTemplate, clientConfig));

    final String expectedMessage = "'orderProperties' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void call_newOrder_isNull_must_throw_IllegalArgumentException() {
    DaasNewOrderDto newOrder = null;
    Exception exception =
        assertThrows(IllegalArgumentException.class, executeOrderClient(newOrder));

    final String expectedMessage = "'newOrder' must not be null";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }



  @Test
  void call_return_4xx_status_code() throws InterruptedException {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId = "phoneId";
    final String phoneName = "phoneName";
    final Double phonePrice = 100.0;

    final Double totalPrice = phonePrice;

    final DaasPhoneOrderDto phone = createPhoneOrderDto(phoneId, phoneName, phonePrice);
    final DaasNewOrderDto newOrderDto =
        createNewOrderDto(customerName, customerSurname, customerEmail, totalPrice, phone);

    final int statusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();

    mockBackEnd.enqueue(new MockResponse().setResponseCode(statusCode));

    Exception exception = assertThrows(RestClientException.class, executeOrderClient(newOrderDto));

    final String expectedMessage = "422 Client Error: [no body]";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);

    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertions(recordedRequest);
  }

  @Test
  void call_return_5xx_status_code() throws InterruptedException {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId = "phoneId";
    final String phoneName = "phoneName";
    final Double phonePrice = 100.0;

    final Double totalPrice = phonePrice;

    final DaasPhoneOrderDto phone = createPhoneOrderDto(phoneId, phoneName, phonePrice);
    final DaasNewOrderDto newOrderDto =
        createNewOrderDto(customerName, customerSurname, customerEmail, totalPrice, phone);

    final int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

    mockBackEnd.enqueue(new MockResponse().setResponseCode(statusCode));

    Exception exception = assertThrows(RestClientException.class, executeOrderClient(newOrderDto));

    final String expectedMessage = "500 Server Error: [no body]";
    final String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);


    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertions(recordedRequest);
  }

  @Test
  void call_return_201_status_code() throws InterruptedException {
    final String orderId = UUID.randomUUID().toString();
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId = "phoneId";
    final String phoneName = "phoneName";
    final Double phonePrice = 100.0;

    final Double totalPrice = phonePrice;

    final DaasPhoneOrderDto phone = createPhoneOrderDto(phoneId, phoneName, phonePrice);
    final DaasNewOrderDto newOrderDto =
        createNewOrderDto(customerName, customerSurname, customerEmail, totalPrice, phone);

    final DaasOrderDto expectedResponse =
        createOrderDto(orderId, customerName, customerSurname, customerEmail, totalPrice, phone);

    final int statusCode = HttpStatus.CREATED.value();

    final String body = createBodyResponse(orderId, customerName, customerSurname, customerEmail,
        phoneId, phoneName, phonePrice, totalPrice);

    mockBackEnd.enqueue(new MockResponse().setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).setBody(body)
        .setResponseCode(statusCode));

    final DaasOrderDto response = this.client.createOrder(newOrderDto);
    assertNotNull(response);
    assertEquals(expectedResponse, response);

    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertions(recordedRequest);

  }

  private static String createBodyResponse(final String orderId, final String customerName,
      final String customerSurname, final String customerEmail, final String phoneId,
      final String phoneName, final Double phonePrice, final Double totalPrice) {
    // @formatter:off
    return String.format(
        "{\n" + 
        "  \"orderId\": \"%s\",\n" + 
        "  \"customer\": {\n" + 
        "    \"name\": \"%s\",\n" + 
        "    \"surname\": \"%s\",\n" + 
        "    \"email\": \"%s\"\n" + 
        "  },\n" + 
        "  \"phones-to-buy\": [\n" + 
        "    {\n" + 
        "      \"phoneId\": \"%s\",\n" + 
        "      \"name\": \"%s\",\n" + 
        "      \"price\": %.2f\n" + 
        "    }\n" + 
        "  ],\n" + 
        "  \"total-price\": %.2f\n" + 
        "}", 
        orderId, customerName, customerSurname, customerEmail,
        phoneId, phoneName, phonePrice, totalPrice);
    // @formatter:on
  }

  private static void assertions(final RecordedRequest recordedRequest) {
    Assertions.assertEquals("POST", recordedRequest.getMethod());
    Assertions.assertEquals(URL_CALLED, recordedRequest.getPath());
  }

  private static DaasOrderDto createOrderDto(final String orderId, final String customerName,
      final String customerSurname, final String customerEmail, final Double totalPrice,
      final DaasPhoneOrderDto... phones) {
    // @formatter:off
    return DaasOrderDto.builder()
        .orderId(orderId)
        .customerData(createCustomerDto(customerName, customerSurname, customerEmail))
        .phoneListToBuy(Arrays.asList(phones))
        .totalPrice(totalPrice)
        .build();
    // @formatter:on
  }

  private static DaasNewOrderDto createNewOrderDto(final String customerName,
      final String customerSurname, final String customerEmail, final Double totalPrice,
      final DaasPhoneOrderDto... phones) {
    // @formatter:off
    return DaasNewOrderDto.builder()
        .customerData(createCustomerDto(customerName, customerSurname, customerEmail))
        .phoneListToBuy(Arrays.asList(phones))
        .totalPrice(totalPrice)
        .build();
    // @formatter:on
  }

  private static DaasCustomerDto createCustomerDto(final String name, final String surname,
      final String email) {
    // @formatter:off
    return DaasCustomerDto.builder()
        .name(name)
        .surname(surname)
        .email(email)
        .build();
    // @formatter:on
  }

  private static DaasPhoneOrderDto createPhoneOrderDto(final String phoneId, final String name,
      final Double price) {
    // @formatter:off
    return DaasPhoneOrderDto.builder()
        .phoneId(phoneId)
        .name(name)
        .price(price)
        .build();
    // @formatter:on
  }

  private Executable executeOrderClient(final DaasNewOrderDto newOrder) {
    return new Executable() {
      @Override
      public void execute() throws Throwable {
        OrderClientImplTest.this.client.createOrder(newOrder);
      }
    };
  }
}
