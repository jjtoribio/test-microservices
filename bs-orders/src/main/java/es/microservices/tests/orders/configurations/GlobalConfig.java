package es.microservices.tests.orders.configurations;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import es.microservices.tests.orders.clients.OrderClient;
import es.microservices.tests.orders.clients.PhoneClient;
import es.microservices.tests.orders.clients.impl.OrderClientImpl;
import es.microservices.tests.orders.clients.impl.PhoneClientImpl;
import es.microservices.tests.orders.configurations.properties.OrderClientProperties;
import es.microservices.tests.orders.configurations.properties.PhoneClientProperties;
import es.microservices.tests.orders.services.OrderService;
import es.microservices.tests.orders.services.impl.OrderServiceImpl;

@Configuration
public class GlobalConfig {

  @Bean
  @LoadBalanced
  public RestTemplate builder() {
    return new RestTemplate();
  }

  @Bean
  public PhoneClient phoneClient(final RestTemplate restTemplate,
      final PhoneClientProperties phoneProperties) {
    return new PhoneClientImpl(restTemplate, phoneProperties);
  }

  @Bean
  public OrderClient orderClient(final RestTemplate restTemplate,
      final OrderClientProperties orderProperties) {
    return new OrderClientImpl(restTemplate, orderProperties);
  }

  @Bean
  public OrderService orderService(PhoneClient phoneClient, OrderClient orderClient) {
    return new OrderServiceImpl(phoneClient, orderClient);
  }

}
