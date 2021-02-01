package es.microservices.tests.orders.dtos;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * NewOrderDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class NewOrderDto {

  /**
   * The phone name
   * 
   * @return name
   **/
  @Schema(required = true, description = "The customer data")
  @NotNull
  @JsonProperty("customer")
  @Valid
  private CustomerDto customerData;

  @Schema(required = true, description = "The list of identifiers of the phones that the customer wants to buy.")
  @Singular(value = "phoneIdToBuy")
  @JsonProperty("phones-to-buy")
  @NotEmpty
  @Valid
  private List<@NotBlank @Valid String> phoneIdListToBuy;

}
