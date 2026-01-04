package org.conalton.textprocessor.infrastructure.concurrency.job;

import java.util.concurrent.*;
import org.conalton.textprocessor.infrastructure.configuration.AsyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BackgroundJobRunner {
  public record JobRunnerConfig(
      long loopDelayLowerBoundaryMs, long loopDelayHigherBoundaryMs, boolean firstRunBeforeDelay) {}

  private static final Logger log = LoggerFactory.getLogger(BackgroundJobRunner.class);
  private final Executor executor;
  private final AsyncConfig.AsyncConfigProperties configProperties;

  public BackgroundJobRunner(
      @Qualifier(AsyncConfig.AsyncJobExecutor.BackgroundJob) Executor executor,
      AsyncConfig.AsyncConfigProperties configProperties) {
    this.executor = executor;
    this.configProperties = configProperties;
  }

  public CompletableFuture<?> runInLoop(String jobName, Runnable task, JobRunnerConfig config) {
    return CompletableFuture.runAsync(
        () -> {
          log.info("Job [{}] started", jobName);

          while (!Thread.currentThread().isInterrupted()) {
            try {
              long delay =
                  ThreadLocalRandom.current()
                      .nextLong(
                          config.loopDelayLowerBoundaryMs, config.loopDelayHigherBoundaryMs + 1);

              if (!config.firstRunBeforeDelay) {
                TimeUnit.MILLISECONDS.sleep(delay);
              }

              task.run();

              if (config.firstRunBeforeDelay) {
                TimeUnit.MILLISECONDS.sleep(delay);
              }

            } catch (InterruptedException ex) {
              log.info("Job [{}] interrupted", jobName);
              Thread.currentThread().interrupt();
              break;
            } catch (Exception ex) {
              log.info("Job [{}] crashed", jobName, ex);

              try {
                TimeUnit.MILLISECONDS.sleep(configProperties.getRestartDelayMs());
              } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
              }
            }
          }
        },
        executor);
  }
}
