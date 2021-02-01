package es.microservices.tests.orders.controllers.handlers;

import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import brave.Span;
import brave.Tracer;
import es.microservices.tests.orders.dtos.ErrorResponse;
import es.microservices.tests.orders.exceptions.CreatingOrderException;
import es.microservices.tests.orders.exceptions.PhoneRequestedListEmptyException;
import es.microservices.tests.orders.exceptions.PhoneRequestedNotFound;

@ControllerAdvice
public class SpecificControllerAdvice {

  @Autowired
  private Tracer tracer;

  @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      HttpServletRequest request, MethodArgumentNotValidException ex) {
    final String errorMessage = errorMessageHandle(ex);
    return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
  }

  private static String errorMessageHandle(final MethodArgumentNotValidException t) {
    final StringBuilder errorMessage = new StringBuilder();

    final List<FieldError> errors = t.getFieldErrors();
    errors.stream().forEach(
        error -> errorMessage.append(error.getField() + ": " + error.getDefaultMessage() + "\n"));
    return errorMessage.toString().trim();
  }

  @ExceptionHandler(CreatingOrderException.class)
  public ResponseEntity<ErrorResponse> handleCreatingOrderException(HttpServletRequest request,
      CreatingOrderException ex) {
    return createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }

  @ExceptionHandler(PhoneRequestedListEmptyException.class)
  public ResponseEntity<ErrorResponse> handlePhoneRequestedListEmptyException(
      HttpServletRequest request, PhoneRequestedListEmptyException ex) {
    return createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }

  @ExceptionHandler(PhoneRequestedNotFound.class)
  public ResponseEntity<ErrorResponse> handlePhoneRequestedNotFound(HttpServletRequest request,
      PhoneRequestedNotFound ex) {
    return createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleHttpClientErrorException(HttpServletRequest request,
      Exception ex) {
    return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  private ResponseEntity<ErrorResponse> createErrorResponse(final HttpStatus status,
      final String errorMessage) {
    return ResponseEntity.status(status)
        .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(ErrorResponse.builder().errorMessage(errorMessage).status(status.value())
            .operationId(getTraceId()).build());
  }

  private String getTraceId() {
    Span currentSpan = tracer.currentSpan();
    if (Objects.isNull(tracer.currentSpan())) {
      currentSpan = tracer.nextSpan().name("test").start();
    }
    return currentSpan.context().traceIdString();

  }

}
