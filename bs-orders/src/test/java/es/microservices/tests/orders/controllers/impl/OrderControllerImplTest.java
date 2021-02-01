package es.microservices.tests.orders.controllers.impl;

import static es.microservices.tests.orders.configurations.utils.TestFeatures.createDaasOrderDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createDaasPhoneCatalogDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createDaasPhoneDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createDaasPhoneOrderDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createNewOrderDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createOrderDto;
import static es.microservices.tests.orders.configurations.utils.TestFeatures.createPhoneOrderDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import es.microservices.tests.orders.clients.OrderClient;
import es.microservices.tests.orders.clients.PhoneClient;
import es.microservices.tests.orders.controllers.handlers.SpecificControllerAdvice;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.PhoneOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasPhoneOrderDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneCatalogDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneDto;
import es.microservices.tests.orders.services.OrderService;
import es.microservices.tests.orders.services.impl.OrderServiceImpl;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderControllerImplTest.InnerConfiguration.class})
@WebMvcTest(controllers = {OrderControllerImpl.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class OrderControllerImplTest {

  private static final String URL = "/orders";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderClient orderClient;

  @Autowired
  private PhoneClient phoneClient;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeAll
  public static void beforeAll() {
    Locale.setDefault(Locale.US);
  }

  @BeforeEach
  public void beforeEach() {
    Mockito.clearInvocations(this.orderClient, this.phoneClient);
  }

  @Test
  void newOrder_emptyPhoneIdList() throws Exception {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final NewOrderDto newOrder = createNewOrderDto(customerName, customerSurname, customerEmail);

    final String errorMessage = "phoneIdListToBuy: must not be empty";

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    // @formatter:off
    mockMvc
        .perform(post(URL)
            .content(objectMapper.writeValueAsString(newOrder))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))            
        .andExpect(status()
            .isBadRequest())
        .andExpect(content()
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").isNumber())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isNotEmpty())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isString())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(errorMessage))
        .andExpect(MockMvcResultMatchers.jsonPath("$.operationId").exists());
    // @formatter:on

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));
  }

  @Test
  void newOrder_call_to_phoneClient_returns_error_400() throws Exception {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";
    final String[] phoneIds = new String[] {"phoneId1", "phoneId2"};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final String errorMessage = "An error occurred while retrieving the phone catalog";

    final HttpStatus status = HttpStatus.BAD_REQUEST;
    doThrow(new HttpClientErrorException(status)).when(this.phoneClient).getPhoneData(anyInt(),
        anyInt());

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    // @formatter:off
    mockMvc
        .perform(post(URL)
            .content(objectMapper.writeValueAsString(newOrder))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))            
        .andExpect(status()
            .isInternalServerError())
        .andExpect(content()
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").isNumber())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isNotEmpty())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isString())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(errorMessage))
        .andExpect(MockMvcResultMatchers.jsonPath("$.operationId").exists());
    // @formatter:on
    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

  }


  @Test
  void newOrder_call_to_phoneClient_returns_catalog_but_a_requested_phoneId_is_missing()
      throws Exception {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId1 = "phoneId1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final String phoneImageURL1 = "phoneImageURL1";
    final Double phonePrice1 = 100.0;

    final String phoneId2 = "phoneId2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final String phoneImageURL2 = "phoneImageURL2";

    final Double phonePrice2 = 600.0;

    final String phoneId3 = "phoneId3";

    final String[] phoneIds = new String[] {phoneId1, phoneId3};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final Integer page = 1;
    final Integer pageSize = 10;
    final Long totalCount = 2l;
    final DaasPhoneDto phone1 =
        createDaasPhoneDto(phoneId1, phoneName1, phoneDescription1, phoneImageURL1, phonePrice1);
    final DaasPhoneDto phone2 =
        createDaasPhoneDto(phoneId2, phoneName2, phoneDescription2, phoneImageURL2, phonePrice2);

    final DaasPhoneCatalogDto catalog =
        createDaasPhoneCatalogDto(page, pageSize, totalCount, phone1, phone2);

    final String errorMessage =
        "Cannot continue with order creation. The following phone identifiers have not been found: phoneId3";

    doReturn(catalog).when(this.phoneClient).getPhoneData(anyInt(), anyInt());

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    // @formatter:off
    mockMvc
        .perform(post(URL)
            .content(objectMapper.writeValueAsString(newOrder))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))            
        .andExpect(status()
            .isUnprocessableEntity())
        .andExpect(content()
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").isNumber())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isNotEmpty())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isString())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(errorMessage))
        .andExpect(MockMvcResultMatchers.jsonPath("$.operationId").exists());
    // @formatter:on

    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

  }


  @Test
  void newOrder_call_to_orderClient_returns_an_error_creating_order() throws Exception {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId1 = "phoneId1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final String phoneImageURL1 = "phoneImageURL1";
    final Double phonePrice1 = 100.0;

    final String phoneId2 = "phoneId2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final String phoneImageURL2 = "phoneImageURL2";

    final Double phonePrice2 = 600.0;

    final String[] phoneIds = new String[] {phoneId1, phoneId2};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final Integer page = 1;
    final Integer pageSize = 10;
    final Long totalCount = 2l;
    final DaasPhoneDto phone1 =
        createDaasPhoneDto(phoneId1, phoneName1, phoneDescription1, phoneImageURL1, phonePrice1);
    final DaasPhoneDto phone2 =
        createDaasPhoneDto(phoneId2, phoneName2, phoneDescription2, phoneImageURL2, phonePrice2);

    final DaasPhoneCatalogDto catalog =
        createDaasPhoneCatalogDto(page, pageSize, totalCount, phone1, phone2);

    final String errorMessage = "An error occurred creating the order";
    final HttpStatus status = HttpStatus.BAD_REQUEST;

    doReturn(catalog).when(this.phoneClient).getPhoneData(anyInt(), anyInt());

    doThrow(new HttpClientErrorException(status)).when(this.orderClient)
        .createOrder(any(DaasNewOrderDto.class));

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    // @formatter:off
    mockMvc
        .perform(post(URL)
            .content(objectMapper.writeValueAsString(newOrder))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))            
        .andExpect(status()
            .isUnprocessableEntity())
        .andExpect(content()
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").isNumber())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isNotEmpty())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").isString())
        .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(errorMessage))
        .andExpect(MockMvcResultMatchers.jsonPath("$.operationId").exists());
    // @formatter:on

    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, times(1)).createOrder(any(DaasNewOrderDto.class));

  }

  @Test
  void createOrderOk() throws JsonProcessingException, Exception {
    final String customerName = "customerName";
    final String customerSurname = "customerSurname";
    final String customerEmail = "email@email.com";

    final String phoneId1 = "phoneId1";
    final String phoneName1 = "phoneName1";
    final String phoneDescription1 = "phoneDescription1";
    final String phoneImageURL1 = "phoneImageURL1";
    final Double phonePrice1 = 100.0;

    final String phoneId2 = "phoneId2";
    final String phoneName2 = "phoneName2";
    final String phoneDescription2 = "phoneDescription2";
    final String phoneImageURL2 = "phoneImageURL2";

    final Double phonePrice2 = 600.0;

    final String[] phoneIds = new String[] {phoneId1, phoneId2};
    final NewOrderDto newOrder =
        createNewOrderDto(customerName, customerSurname, customerEmail, phoneIds);

    final Integer page = 1;
    final Integer pageSize = 10;
    final Long totalCount = 2l;
    final DaasPhoneDto phone1 =
        createDaasPhoneDto(phoneId1, phoneName1, phoneDescription1, phoneImageURL1, phonePrice1);
    final DaasPhoneDto phone2 =
        createDaasPhoneDto(phoneId2, phoneName2, phoneDescription2, phoneImageURL2, phonePrice2);

    final DaasPhoneCatalogDto catalog =
        createDaasPhoneCatalogDto(page, pageSize, totalCount, phone1, phone2);

    final String orderId = "orderId";

    final DaasPhoneOrderDto daasPhoneOrder1 =
        createDaasPhoneOrderDto(phoneId1, phoneName1, phonePrice1);
    final DaasPhoneOrderDto daasPhoneOrder2 =
        createDaasPhoneOrderDto(phoneId2, phoneName2, phonePrice2);

    final DaasOrderDto orderDto = createDaasOrderDto(orderId, customerName, customerSurname,
        customerEmail, phonePrice1 + phonePrice2, daasPhoneOrder1, daasPhoneOrder2);

    final PhoneOrderDto phoneOrder1 = createPhoneOrderDto(phoneId1, phoneName1, phonePrice1);
    final PhoneOrderDto phoneOrder2 = createPhoneOrderDto(phoneId2, phoneName2, phonePrice2);
    final OrderDto orderDtoExpected = createOrderDto(orderId, customerName, customerSurname,
        customerEmail, phonePrice1 + phonePrice2, phoneOrder1, phoneOrder2);

    doReturn(catalog).when(this.phoneClient).getPhoneData(anyInt(), anyInt());
    doReturn(orderDto).when(this.orderClient).createOrder(any(DaasNewOrderDto.class));

    verify(this.phoneClient, never()).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, never()).createOrder(any(DaasNewOrderDto.class));

    // @formatter:off
    mockMvc
        .perform(post(URL)
            .content(objectMapper.writeValueAsString(newOrder))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))            
        .andExpect(status()
            .isCreated())
        .andExpect(content()
            .contentType(MediaType.APPLICATION_JSON_VALUE))
         .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").isString())
         .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(orderDtoExpected.getOrderId()))
         .andExpect(MockMvcResultMatchers.jsonPath("$.customer").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.customer").isMap())
         .andExpect(MockMvcResultMatchers.jsonPath("$.customer").value(orderDtoExpected.getCustomerData()))
         .andExpect(MockMvcResultMatchers.jsonPath("$.phones-to-buy").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.phones-to-buy").isArray())
         .andExpect(MockMvcResultMatchers.jsonPath("$.phones-to-buy[0]").value(orderDtoExpected.getPhoneListToBuy().get(0)))
         .andExpect(MockMvcResultMatchers.jsonPath("$.phones-to-buy[1]").value(orderDtoExpected.getPhoneListToBuy().get(1)))
         .andExpect(MockMvcResultMatchers.jsonPath("$.total-price").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.total-price").isNumber())
         .andExpect(MockMvcResultMatchers.jsonPath("$.total-price").value(orderDtoExpected.getTotalPrice()))
         
         ;
    // @formatter:on

    verify(this.phoneClient, times(1)).getPhoneData(anyInt(), anyInt());
    verify(this.orderClient, times(1)).createOrder(any(DaasNewOrderDto.class));

  }

  @TestConfiguration
  @EnableAutoConfiguration
  @Import({OrderControllerImpl.class, SpecificControllerAdvice.class})
  static class InnerConfiguration {

    @Bean
    public OrderClient mockOrderClient() {
      return mock(OrderClient.class);
    }

    @Bean
    public PhoneClient mockPhoneClient() {
      return mock(PhoneClient.class);
    }

    @Bean
    public OrderService orderService(PhoneClient phoneClient, OrderClient orderClient) {
      return new OrderServiceImpl(phoneClient, orderClient);
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
