package org.conalton.textprocessor.modules.task.entity;

import jakarta.persistence.*;
import java.time.Instant;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "tasks")
public final class Task implements Persistable<String> {
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "source_path", nullable = false)
  private String sourcePath;

  @Column(name = "result_path")
  private String resultPath;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(columnDefinition = "JSON")
  private String meta;

  @Transient private boolean isNew = false;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSourcePath() {
    return sourcePath;
  }

  public void setSourcePath(String sourcePath) {
    this.sourcePath = sourcePath;
  }

  public String getResultPath() {
    return resultPath;
  }

  public void setResultPath(String resultPath) {
    this.resultPath = resultPath;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(Instant finishedAt) {
    this.finishedAt = finishedAt;
  }

  public String getMeta() {
    return meta;
  }

  public void setMeta(String meta) {
    this.meta = meta;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  public void markAsNew() {
    this.isNew = true;
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    this.isNew = false;
  }
}
