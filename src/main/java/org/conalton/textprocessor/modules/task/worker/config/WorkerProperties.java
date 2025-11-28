package org.conalton.textprocessor.modules.task.worker.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "modules.task.worker")
public class WorkerProperties {
  @Min(1)
  @Max(12)
  private int numWorkers;

  @Min(10)
  @Max(2000)
  private int startDelayRangeMsMin;

  @Min(10)
  @Max(2000)
  private int startDelayRangeMsMax;

  @Min(100)
  @Max(5000)
  private int loopDelayRangeMsMin;

  @Min(100)
  @Max(5000)
  private int loopDelayRangeMsMax;

  @Min(100)
  @Max(5000)
  private int shutdownDelayMs;

  @AssertTrue(message = "startDelayRangeMsMax must be greater than startDelayRangeMsMin")
  public boolean isStartDelayRangeValid() {
    return startDelayRangeMsMax > startDelayRangeMsMin;
  }

  @AssertTrue(message = "loopDelayRangeMsMax must be greater than loopDelayRangeMsMin")
  public boolean isLoopDelayRangeValid() {
    return loopDelayRangeMsMax > loopDelayRangeMsMin;
  }

  public int getNumWorkers() {
    return numWorkers;
  }

  public void setNumWorkers(int numWorkers) {
    this.numWorkers = numWorkers;
  }

  public int getStartDelayRangeMsMin() {
    return startDelayRangeMsMin;
  }

  public void setStartDelayRangeMsMin(int startDelayRangeMsMin) {
    this.startDelayRangeMsMin = startDelayRangeMsMin;
  }

  public int getStartDelayRangeMsMax() {
    return startDelayRangeMsMax;
  }

  public void setStartDelayRangeMsMax(int startDelayRangeMsMax) {
    this.startDelayRangeMsMax = startDelayRangeMsMax;
  }

  public int getLoopDelayRangeMsMin() {
    return loopDelayRangeMsMin;
  }

  public void setLoopDelayRangeMsMin(int loopDelayRangeMsMin) {
    this.loopDelayRangeMsMin = loopDelayRangeMsMin;
  }

  public int getLoopDelayRangeMsMax() {
    return loopDelayRangeMsMax;
  }

  public void setLoopDelayRangeMsMax(int loopDelayRangeMsMax) {
    this.loopDelayRangeMsMax = loopDelayRangeMsMax;
  }

  public int getShutdownDelayMs() {
    return shutdownDelayMs;
  }

  public void setShutdownDelayMs(int shutdownDelayMs) {
    this.shutdownDelayMs = shutdownDelayMs;
  }
}
