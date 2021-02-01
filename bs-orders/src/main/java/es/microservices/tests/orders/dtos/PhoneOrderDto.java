package es.microservices.tests.orders.dtos;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PhoneOrderDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class PhoneOrderDto {

  /**
   * The phone identifier
   * 
   * @return phoneId
   **/
  @Schema(required = true, description = "The phone identifier")
  @JsonProperty("phoneId")
  @NotBlank
  private String phoneId;

  /**
   * The phone name
   * 
   * @return name
   **/
  @Schema(required = true, description = "The phone name")
  @NotBlank
  @JsonProperty("name")
  private String name;

  /**
   * The phone price minimum: 0
   * 
   * @return price
   **/
  @DecimalMin(value = "0", message = "the price cannot be less than 0")
  @JsonProperty("price")
  @NotNull
  private Double price;
  
}
