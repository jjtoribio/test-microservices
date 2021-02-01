package es.microservices.tests.orders.dtos;

import java.util.List;
import javax.validation.Valid;
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
 * PhoneCatalogDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class OrderListDto {

  /**
   * The phone list
   * @return phones
   **/
  @Schema(required = true, description = "The order list")
  @NotNull
  @JsonProperty("orders")
  @Valid
  @Singular
  private List<@NotNull OrderDto> orders;

  /**
   * Get limit
   * @return limit
   **/
  @Schema(required = true, description = "Indicates how many results the query should return at most. Default 10")
  @JsonProperty("pageSize")
  @Builder.Default
  @NotNull
  private Integer pageSize = Integer.valueOf(10);

  @Schema(required = true, description = "The current page number. Start in 1")
  @JsonProperty("page")
  @NotNull
  @Builder.Default 
  private Integer page = Integer.valueOf(1);

  /**
   * Get totalCount
   * @return totalCount
   **/
  @Schema(required = true, description = "Indicates the total number of records that have been returned")      
  @JsonProperty("total-count")
  @NotNull
  private Long totalCount;
  
}
