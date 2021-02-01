package es.microservices.tests.phones.dtos;

import javax.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ErrorResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class ErrorResponse {

  /**
   * The error message
   * 
   * @return errorMessage
   **/
  @Schema(required = true, description = "The http status code")
  @JsonProperty("status")
  private int status;
  
  @Schema(required = true, description = "The error message")
  @NotBlank
  @JsonProperty("errorMessage")
  private String errorMessage;
  
  @Schema(required = true, description = "The operation id")
  @NotBlank
  @JsonProperty("operationId")
  private String operationId;
  
}
