package org.conalton.textprocessor.dto.response.common;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String message) implements ApiResponse {}
