package es.microservices.tests.orders.clients.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import es.microservices.tests.orders.clients.PhoneClient;
import es.microservices.tests.orders.configurations.properties.PhoneClientProperties;
import es.microservices.tests.orders.dtos.phones.DaasPhoneCatalogDto;

public class PhoneClientImpl implements PhoneClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String endpointUrl;

  public PhoneClientImpl(final RestTemplate restTemplate,
      final PhoneClientProperties phoneProperties) {
    Assert.notNull(restTemplate, "'restTemplate' must not be null");
    Assert.notNull(phoneProperties, "'phoneProperties' must not be null");
    Assert.hasText(phoneProperties.getBaseUrl(),
        "'phoneProperties.baseUrl' must not be null or empty");
    Assert.hasText(phoneProperties.getEndpointUrl(),
        "'phoneProperties.endpointUrl' must not be null or empty");
    this.restTemplate = restTemplate;
    this.baseUrl = phoneProperties.getBaseUrl();    
    this.endpointUrl = phoneProperties.getEndpointUrl();
  }


  @Override
  public DaasPhoneCatalogDto getPhoneData(final Integer page, final Integer pageSize) {
    Assert.notNull(page, "'page' must not be null");
    Assert.notNull(pageSize, "'pageSize' must not be null");
    Assert.isTrue(page > 0, "'page' must be greater than zero");
    Assert.isTrue(pageSize > 0, "'pageSize' must be greater than zero");

    // @formatter:off    
    final HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.baseUrl + "/" + this.endpointUrl)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize);

    final HttpEntity<?> entity = new HttpEntity<>(headers);

    final ResponseEntity<DaasPhoneCatalogDto> response = restTemplate.exchange(
            builder.toUriString(), 
            HttpMethod.GET, 
            entity, 
            DaasPhoneCatalogDto.class);    
    return response.getBody();
     // @formatter:on
  }


}
