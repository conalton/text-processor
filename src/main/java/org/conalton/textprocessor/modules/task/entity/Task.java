package org.conalton.textprocessor.modules.task.entity;

import jakarta.persistence.*;
import java.time.Instant;
import org.conalton.textprocessor.common.annotation.Internal;
import org.conalton.textprocessor.modules.task.service.TaskStatusFlowService;
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
  @Column(name = "status", nullable = false, length = 32)
  private TaskStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_prev", length = 32)
  private TaskStatus statusPrev;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(columnDefinition = "JSON")
  private String meta;

  @Column(name = "attempt_current", nullable = false)
  private Integer attemptCurrent = 0;

  @Column(name = "attempt_max", nullable = false)
  private Integer attemptMax = 3;

  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  @Column(name = "priority", nullable = false)
  private Integer priority = 0;

  @Column(name = "locked_by")
  private String lockedBy;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @Column(name = "heartbeat_at")
  private Instant heartbeatAt;

  @Column(name = "cancel_at")
  private Instant cancelAt;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

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

  @Internal(allowedBy = TaskStatusFlowService.class)
  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  public TaskStatus getStatusPrev() {
    return statusPrev;
  }

  public void setStatusPrev(TaskStatus statusPrev) {
    this.statusPrev = statusPrev;
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

  public Integer getAttemptCurrent() {
    return attemptCurrent;
  }

  public void setAttemptCurrent(Integer attemptCurrent) {
    this.attemptCurrent = attemptCurrent;
  }

  public Integer getAttemptMax() {
    return attemptMax;
  }

  public void setAttemptMax(Integer attemptMax) {
    this.attemptMax = attemptMax;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public void setNextAttemptAt(Instant nextAttemptAt) {
    this.nextAttemptAt = nextAttemptAt;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
  }

  public Instant getLockedAt() {
    return lockedAt;
  }

  public void setLockedAt(Instant lockedAt) {
    this.lockedAt = lockedAt;
  }

  public Instant getHeartbeatAt() {
    return heartbeatAt;
  }

  public void setHeartbeatAt(Instant heartbeatAt) {
    this.heartbeatAt = heartbeatAt;
  }

  public Instant getCancelAt() {
    return cancelAt;
  }

  public void setCancelAt(Instant cancelAt) {
    this.cancelAt = cancelAt;
  }

  public Long getVersion() {
    return version;
  }

  public void unlock() {
    this.lockedAt = null;
    this.lockedBy = null;
  }

  private void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  public void markEntityAsNew() {
    this.isNew = true;
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    this.isNew = false;
  }
}
