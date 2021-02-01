package es.microservices.tests.phones.configurations;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI(Map<String, SecurityScheme> securitySchemes) {
    // @formatter:off
    return new OpenAPI()
        .components(new Components())
        .info(new Info()
            .title("Phone Catalog operations")
            .description("Everything about your phone catalog")
            .version("v1-0")
            .license(new License()
                .url(""))
            .contact(new Contact()
                .email("myself@gmail.com")));
    // @formatter:on
  }


}
