package es.microservices.tests.phones.dtos;

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
 * NewPhoneDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class NewPhoneDto {

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
   * The phone description
   * 
   * @return description
   **/
  @Schema(required = true, description = "The phone description")
  @NotBlank
  @JsonProperty("description")
  private String description;

  /**
   * The phone price minimum: 0
   * 
   * @return price
   **/
  @DecimalMin(value = "0", message = "the price cannot be less than 0")
  @NotNull
  @JsonProperty("price")
  private Double price;

  /**
   * A URL with the phone image
   * 
   * @return imageURL
   **/
  @Schema(required = true, description = "A URL with the phone image")
  @NotBlank
  @JsonProperty("imageURL")
  private String imageURL;
  
}
