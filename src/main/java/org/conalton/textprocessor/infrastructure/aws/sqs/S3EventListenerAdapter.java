package org.conalton.textprocessor.infrastructure.aws.sqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.conalton.textprocessor.domain.messaging.port.FileStorageEventListenerPort;
import org.conalton.textprocessor.domain.storage.types.FileStorageItem;

/** Parse incoming SQS messages with S3 event notifications. */
public class S3EventListenerAdapter implements FileStorageEventListenerPort {
  private final ObjectMapper objectMapper;

  public S3EventListenerAdapter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<FileStorageItem> onJsonPayload(String payload) {
    try {
      JsonNode root = objectMapper.readTree(payload);
      JsonNode records = root.path("Records");

      if (!records.isArray()) {
        return Collections.emptyList();
      }

      List<FileStorageItem> result = new ArrayList<>(records.size());
      for (JsonNode record : records) {
        String bucket = record.path("s3").path("bucket").path("name").asText(null);
        String rawKey = record.path("s3").path("object").path("key").asText(null);

        if (bucket == null || rawKey == null || rawKey.isBlank() || bucket.isBlank()) {
          continue;
        }

        String decodedKey = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
        result.add(new FileStorageItem(bucket, decodedKey));
      }

      return List.copyOf(result);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot parse S3 event payload", e);
    }
  }
}
