package org.conalton.textprocessor.domain.messaging.interfaces;

import org.conalton.textprocessor.domain.messaging.types.MessageEnvelope;

@FunctionalInterface
public interface MessageHandler {
  void handle(MessageEnvelope envelope);
}
