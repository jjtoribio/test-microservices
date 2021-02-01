package es.microservices.tests.orders.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CustomerDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CustomerDto {

  @Schema(required = true, description = "The customer name")
  @NotBlank
  @JsonProperty("name")
  private String name;
  
  @Schema(required = true, description = "The customer surname")
  @NotBlank
  @JsonProperty("surname")
  private String surname;
  
  @Schema(required = true, description = "The customer email")
  @NotBlank
  @Email
  @JsonProperty("email")
  private String email;
}
