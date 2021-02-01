package es.microservices.tests.orders.configurations.properties;

import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Validated
@Configuration
@ConfigurationProperties(prefix = "es.microservices.tests.orders.phones-config")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class PhoneClientProperties {

  @Getter
  @Setter
  @NotBlank
  private String baseUrl;


  @Getter
  @Setter
  @NotBlank
  private String endpointUrl;


}
