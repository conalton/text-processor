package org.conalton.textprocessor.modules.task.entity;

public enum TaskStatus {
  PENDING("pending"),
  PROCESSING("processing"),
  COMPLETED("completed"),
  FAILED("failed");

  private final String description;

  TaskStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
