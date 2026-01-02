package org.conalton.textprocessor.modules.task.worker;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import org.conalton.textprocessor.infrastructure.concurrency.job.BackgroundJobRunner;
import org.conalton.textprocessor.modules.task.worker.config.WorkerProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class TaskWorkerLifecycle implements SmartLifecycle {
  private final BackgroundJobRunner backgroundJobRunner;
  private final WorkerProperties workerProperties;
  private volatile boolean running;
  private final ArrayList<CompletableFuture<?>> jobs = new ArrayList<>();

  public TaskWorkerLifecycle(
      BackgroundJobRunner backgroundJobRunner, WorkerProperties workerProperties) {
    this.backgroundJobRunner = backgroundJobRunner;
    this.workerProperties = workerProperties;
  }

  @Override
  public void start() {
    for (var i = 0; i < workerProperties.getNumWorkers(); i++) {
      jobs.add(
          backgroundJobRunner.runInLoop(
              "tasks.worker-" + i,
              new Task(i),
              new BackgroundJobRunner.JobRunnerConfig(
                  workerProperties.getLoopDelayRangeMsMin(),
                  workerProperties.getLoopDelayRangeMsMax(),
                  true)));
    }

    running = true;
  }

  @Override
  public void stop() {
    jobs.forEach(job -> job.cancel(true));
    jobs.clear();
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
