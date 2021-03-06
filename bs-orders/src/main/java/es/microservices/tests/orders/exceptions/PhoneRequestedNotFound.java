package es.microservices.tests.orders.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class PhoneRequestedNotFound extends RuntimeException{

  public PhoneRequestedNotFound(final String message) {
    super(message);
  }
}
