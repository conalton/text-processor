package org.conalton.textprocessor.web.advice.common;

import org.conalton.textprocessor.web.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
    log.error("Unhandled exception occurred", ex);
    return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.error("Illegal argument exception occurred", ex);
    return createErrorResponse(HttpStatus.BAD_REQUEST, "Illegal argument");
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
    log.error("Illegal state exception occurred", ex);
    return createErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal server error (Illegal state exception occurred).");
  }
}
