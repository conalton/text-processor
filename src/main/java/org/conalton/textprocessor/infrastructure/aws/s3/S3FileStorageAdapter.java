package org.conalton.textprocessor.infrastructure.aws.s3;

import java.time.Duration;
import org.conalton.textprocessor.domain.service.storage.FileStoragePort;
import org.conalton.textprocessor.domain.service.storage.StorageLocation;
import org.conalton.textprocessor.domain.service.storage.StorageProperties;
import org.conalton.textprocessor.dto.internal.PresignedUrlData;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class S3FileStorageAdapter implements FileStoragePort {
  private final S3Presigner s3Presigner;
  private final StorageProperties storageProps;

  public S3FileStorageAdapter(S3Presigner s3Presigner, StorageProperties storageProps) {
    this.s3Presigner = s3Presigner;
    this.storageProps = storageProps;
  }

  @Override
  public PresignedUrlData generatePresignedUploadUrl(StorageLocation location, String uploadPath) {
    String bucketName =
        switch (location) {
          case TASKS -> storageProps.getTasksBucketName();
        };

    if (bucketName == null || bucketName.isBlank()) {
      throw new IllegalArgumentException("Unsupported storage location: " + location);
    }

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().bucket(bucketName).key(uploadPath).build();

    PutObjectPresignRequest putObjectPresignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(storageProps.getPresignedUrlExpirationMinutes()))
            .putObjectRequest(putObjectRequest)
            .build();

    PresignedPutObjectRequest presignedRequest =
        s3Presigner.presignPutObject(putObjectPresignRequest);

    return new PresignedUrlData(presignedRequest.url().toString(), uploadPath);
  }
}
