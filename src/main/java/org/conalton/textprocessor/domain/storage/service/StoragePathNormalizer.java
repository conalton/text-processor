package org.conalton.textprocessor.domain.storage.service;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class StoragePathNormalizer {
  public Optional<String> normalize(String path) {
    if (path == null) {
      return Optional.empty();
    }

    if (path.isBlank()) {
      return Optional.of("");
    }

    return Optional.of(path.startsWith("/") ? path.substring(1) : path);
  }
}
