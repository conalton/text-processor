package org.conalton.textprocessor.controller.advice.common;

import java.time.Instant;
import org.conalton.textprocessor.dto.response.common.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseExceptionHandler {
  protected static final Logger log = LoggerFactory.getLogger(BaseExceptionHandler.class);

  protected ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(Instant.now(), status.value(), message));
  }
}
