package es.microservices.tests.orders.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class RetrievingPhoneException extends RuntimeException {

  public RetrievingPhoneException(final String message, final Throwable t) {
    super(message, t);
  }
  
}
