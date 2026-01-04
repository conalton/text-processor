package org.conalton.textprocessor.modules.task.worker.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "modules.task.worker")
public class WorkerProperties {
  @Min(0)
  @Max(24)
  private int numWorkers;

  @Min(100)
  @Max(5000)
  private int loopDelayRangeMsMin;

  @Min(100)
  @Max(5000)
  private int loopDelayRangeMsMax;

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
}
