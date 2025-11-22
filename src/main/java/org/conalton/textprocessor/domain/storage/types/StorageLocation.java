package org.conalton.textprocessor.domain.storage.types;

public enum StorageLocation {
  TASKS("tasks", "uploads");

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
