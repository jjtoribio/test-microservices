package es.microservices.tests.phones.controllers.handler;

import java.util.List;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.support.WebExchangeBindException;

@ControllerAdvice
public class SpecificControllerAdvice {

  @ExceptionHandler(ConstraintViolationException.class)
  public HttpStatus handleConstraintViolationException(final ConstraintViolationException e) {
    final HttpStatus httpStatusToReturn = HttpStatus.BAD_REQUEST;
    throw new ResponseStatusException(httpStatusToReturn, errorMessageHandle(e, httpStatusToReturn),
        e);
  }

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

  private static String errorMessageHandle(final Throwable t, final HttpStatus httpStatus) {
    final String originalErrorMessage = t.getMessage();
    final StringBuilder errorMessage = new StringBuilder();

    if (StringUtils.isBlank(originalErrorMessage)) {
      errorMessage.append(httpStatus.getReasonPhrase());
    } else if (originalErrorMessage.contains("\"")) {
      errorMessage.append(originalErrorMessage.replaceAll("\"", "'"));
    } else {
      errorMessage.append(originalErrorMessage);
    }
    return errorMessage.toString();
  }
}
