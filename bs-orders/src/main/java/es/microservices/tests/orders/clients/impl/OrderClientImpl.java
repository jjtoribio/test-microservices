package es.microservices.tests.orders.clients.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import es.microservices.tests.orders.clients.OrderClient;
import es.microservices.tests.orders.configurations.properties.OrderClientProperties;
import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;

public class OrderClientImpl implements OrderClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String endpointUrl;

  public OrderClientImpl(final RestTemplate restTemplate,
      final OrderClientProperties orderProperties) {
    Assert.notNull(restTemplate, "'restTemplate' must not be null");
    Assert.notNull(orderProperties, "'orderProperties' must not be null");
    Assert.hasText(orderProperties.getBaseUrl(),
        "'orderProperties.baseUrl' must not be null or empty");
    Assert.hasText(orderProperties.getEndpointUrl(),
        "'orderProperties.endpointUrl' must not be null or empty");
    this.restTemplate = restTemplate;
    this.baseUrl = orderProperties.getBaseUrl();
    this.endpointUrl = orderProperties.getEndpointUrl();
  }


  @Override
  public DaasOrderDto createOrder(final DaasNewOrderDto newOrder) {
    Assert.notNull(newOrder, "'newOrder' must not be null");
    final UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(this.baseUrl + "/" + this.endpointUrl);

    final HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    final HttpEntity<DaasNewOrderDto> request = new HttpEntity<>(newOrder, headers);

    final ResponseEntity<DaasOrderDto> response =
        restTemplate.postForEntity(builder.toUriString(), request, DaasOrderDto.class);
    return response.getBody();
  }


}
