package org.conalton.textprocessor.domain.messaging.types;

import java.util.Map;

public record MessageEnvelope(String payload, Map<String, Object> attributes) {}
