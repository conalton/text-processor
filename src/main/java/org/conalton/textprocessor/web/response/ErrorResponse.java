package org.conalton.textprocessor.web.response;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String message) implements ApiResponse {}
