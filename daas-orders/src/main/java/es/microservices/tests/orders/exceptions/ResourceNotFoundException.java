package es.microservices.tests.orders.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException() {
    this("Resource not found!");
  }

  public ResourceNotFoundException(String message) {
    this(message, null);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
