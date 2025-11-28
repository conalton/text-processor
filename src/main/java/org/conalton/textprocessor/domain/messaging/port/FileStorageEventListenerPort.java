package org.conalton.textprocessor.domain.messaging.port;

import java.util.List;
import org.conalton.textprocessor.domain.storage.types.FileStorageItem;

public interface FileStorageEventListenerPort {
  public List<FileStorageItem> onJsonPayload(String payload);
}
