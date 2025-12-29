package org.conalton.textprocessor.modules.task.worker.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.conalton.textprocessor.modules.task.worker.config.WorkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class Scheduler implements SmartLifecycle {

  private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

  private final WorkerProperties config;

  private final Lock lifecycleLock = new ReentrantLock();
  private volatile boolean running;
  private ExecutorService executor;

  public Scheduler(WorkerProperties config) {
    this.config = config;
  }

  @Override
  public void start() {
    int min = config.getStartDelayRangeMsMin();
    int max = config.getStartDelayRangeMsMax();
    int loopDelayMin = config.getLoopDelayRangeMsMin();
    int loopDelayMax = config.getLoopDelayRangeMsMax();
    int numWorkers = config.getNumWorkers();

    ExecutorService localExecutor;

    lifecycleLock.lock();
    try {
      if (running) {
        return;
      }
      running = true;
      localExecutor = Executors.newFixedThreadPool(numWorkers);
      executor = localExecutor;
    } finally {
      lifecycleLock.unlock();
    }

    log.info("Scheduler starting with {} workers", numWorkers);

    try {
      for (int i = 0; i < numWorkers; i++) {
        localExecutor.submit(
            () -> {
              long delay = ThreadLocalRandom.current().nextLong(min, max + 1);
              try {
                TimeUnit.MILLISECONDS.sleep(delay);
              } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
              }
              runWorker(loopDelayMin, loopDelayMax);
            });
      }
    } catch (Exception ex) {
      log.error("Failed to submit worker task. Rolling back scheduler start.", ex);
      stop();
      throw ex;
    }
  }

  @Override
  public void stop() {
    ExecutorService toShutdown;

    lifecycleLock.lock();

    try {
      if (!running) {
        return;
      }

      running = false;
      toShutdown = executor;
      executor = null;
    } finally {
      lifecycleLock.unlock();
    }

    if (toShutdown == null) {
      return;
    }

    log.info("Scheduler stopping");
    toShutdown.shutdownNow();

    try {
      if (!toShutdown.awaitTermination(config.getShutdownDelayMs(), TimeUnit.MILLISECONDS)) {
        log.warn("Workers did not terminate within timeout");
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while waiting for workers to stop", ex);
    }
  }

  @Override
  public boolean isRunning() {
    lifecycleLock.lock();

    try {
      return running && executor != null && !executor.isShutdown();
    } finally {
      lifecycleLock.unlock();
    }
  }

  protected void runWorker(int delayMin, int delayMax) {
    log.info("Task-worker [{}] started.", Thread.currentThread().threadId());

    while (!Thread.currentThread().isInterrupted()) {
      if (!running) {
        break;
      }

      try {
        long delay = ThreadLocalRandom.current().nextLong(delayMin, delayMax + 1);
        TimeUnit.MILLISECONDS.sleep(delay);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
