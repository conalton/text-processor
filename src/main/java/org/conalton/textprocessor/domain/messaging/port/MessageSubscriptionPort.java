package org.conalton.textprocessor.domain.messaging.port;

import org.conalton.textprocessor.domain.messaging.interfaces.MessageHandler;

public interface MessageSubscriptionPort {
  void subscribe(String queue, MessageHandler handler);
}
