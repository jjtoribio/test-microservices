package es.microservices.tests.orders.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class CreatingOrderException extends RuntimeException {
  
  public CreatingOrderException(final String message, final Throwable t) {
    super(message, t);
  }

}