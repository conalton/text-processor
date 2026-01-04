package org.conalton.textprocessor.domain.storage.types;

public enum StorageLocation {
  TASKS_PRESIGNED_UPLOADS("tasks", "presigned-uploads"),
  TASKS_UPLOADED_FILES("tasks", "uploaded-files");

  private final String description;
  private final String uploadPrefix;

  StorageLocation(String description, String uploadPrefix) {
    this.description = description;
    this.uploadPrefix = uploadPrefix;
  }

  public String getDescription() {
    return description;
  }

  public String getUploadPrefix() {
    return uploadPrefix;
  }
}
