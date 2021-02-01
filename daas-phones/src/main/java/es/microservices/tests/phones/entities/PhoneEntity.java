package es.microservices.tests.phones.entities;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "phonesCatalog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class PhoneEntity {

  @Id
  @NotBlank
  private String id;  
  
  @NotBlank
  private String name;
  
  @NotBlank
  private String description;
  
  private double price;
  
  @NotNull
  private String imageURL;
  
}
