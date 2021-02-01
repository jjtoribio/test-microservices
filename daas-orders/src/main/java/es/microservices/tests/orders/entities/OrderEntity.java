package es.microservices.tests.orders.entities;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class OrderEntity {

  @Id
  @NotBlank
  private String id;

  /**
   * The phone name
   * 
   * @return name
   **/
  @Schema(required = true, description = "The customer data")
  @NotNull
  @Valid
  private CustomerData customerData;

  @Schema(required = true, description = "The list of the phones that the customer wants to buy.")
  @Singular(value = "phoneToBuy")
  @NotEmpty
  private List<@NotNull @Valid PhoneData> phoneListToBuy;

  @Schema(required = true, description = "The total price to the order")
  @NotNull
  private Double totalPrice;

  /**
   * CustomerDto
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Validated
  public static class CustomerData {

    @Schema(required = true, description = "The customer name")
    @NotBlank
    private String name;

    @Schema(required = true, description = "The customer surname")
    @NotBlank
    private String surname;

    @Schema(required = true, description = "The customer email")
    @NotBlank
    @Email
    private String email;
  }

  /**
   * PhoneDto
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Validated
  public static class PhoneData {

    /**
     * The phone identifier
     * 
     * @return phoneId
     **/
    @Schema(required = true, description = "The phone identifier")
    @NotBlank
    private String phoneId;

    /**
     * The phone name
     * 
     * @return name
     **/
    @Schema(required = true, description = "The phone name")
    @NotBlank
    private String name;

    /**
     * The phone price minimum: 0
     * 
     * @return price
     **/
    @DecimalMin(value = "0", message = "the price cannot be less than 0")
    @NotNull
    private Double price;

  }

}
