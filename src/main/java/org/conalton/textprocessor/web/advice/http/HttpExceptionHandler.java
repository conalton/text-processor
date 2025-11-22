package org.conalton.textprocessor.web.advice.http;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.conalton.textprocessor.web.advice.common.BaseExceptionHandler;
import org.conalton.textprocessor.web.response.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpExceptionHandler extends BaseExceptionHandler {
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found: path={}, method={}", ex.getResourcePath(), request.getMethod());
    return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    log.warn("Validation failed: {}", ex.getMessage());
    return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("Method not allowed: {}", ex.getMessage());
    return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(HttpMessageNotReadableException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return createErrorResponse(HttpStatus.BAD_REQUEST, "Malformed request");
  }

  @ExceptionHandler(RequestNotPermitted.class)
  public ResponseEntity<ErrorResponse> handleRequestNotPermitted(RequestNotPermitted ex) {
    log.warn("Rate limit exceeded: {}", ex.getMessage());
    return createErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
  }
}
