package es.microservices.tests.orders.controllers.handler;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class SpecificControllerAdvice {

  @ExceptionHandler(WebExchangeBindException.class)
  public HttpStatus handleWebExchangeBindException(final WebExchangeBindException e) {
    final HttpStatus httpStatusToReturn = HttpStatus.BAD_REQUEST;
    throw new ResponseStatusException(httpStatusToReturn, errorMessageHandle(e, httpStatusToReturn),
        e);
  }

  private static String errorMessageHandle(final WebExchangeBindException t, final HttpStatus httpStatus) {
      final StringBuilder errorMessage = new StringBuilder();

      final List<FieldError> errors =t.getFieldErrors();
      errors.stream().forEach(error -> 
        errorMessage.append(error.getField() + ": " + error.getDefaultMessage() + "\n")
      );
      return errorMessage.toString().trim();
  }

}
