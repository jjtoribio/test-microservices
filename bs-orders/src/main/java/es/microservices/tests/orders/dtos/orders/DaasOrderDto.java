package es.microservices.tests.orders.dtos.orders;

import java.util.List;
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
 * DaasOrderDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class DaasOrderDto {

  /**
   * The order identifier
   * 
   * @return orderId
   **/
  @Schema(required = true, description = "The order identifier")
  @JsonProperty("orderId")
  private String orderId;

  /**
   * The phone name
   * 
   * @return name
   **/
  @Schema(required = true, description = "The customer data")
  @NotNull
  @JsonProperty("customer")
  private DaasCustomerDto customerData;
  
  @Schema(required = true, description = "The list of identifiers of the phones that the customer wants to buy.")
  @Singular(value = "phoneToBuy")
  @JsonProperty("phones-to-buy")
  @NotEmpty  
  private List<@NotNull DaasPhoneOrderDto> phoneListToBuy;

  
  @Schema(required = true, description = "The total price to the order")
  @JsonProperty("total-price")
  @NotNull
  private Double totalPrice;
}
