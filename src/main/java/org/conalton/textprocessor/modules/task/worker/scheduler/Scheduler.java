package org.conalton.textprocessor.modules.task.worker.scheduler;

import java.util.concurrent.*;
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

    lifecycleLock.lock();

    try {
      if (running) {
        return;
      }

      log.info("Scheduler starting with {} workers", numWorkers);

      Thread.Builder builder = Thread.ofVirtual().name("task-scheduler-", 0);

      executor = Executors.newThreadPerTaskExecutor(builder.factory());

      for (int i = 0; i < numWorkers; i++) {
        executor.submit(
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

      running = true;
    } catch (Exception ex) {
      log.error("Something went wrong. Roll back the ExecutorService state.", ex);
      doStop();
      throw ex;
    } finally {
      lifecycleLock.unlock();
    }
  }

  @Override
  public void stop() {
    lifecycleLock.lock();

    try {
      doStop();
    } finally {
      lifecycleLock.unlock();
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
    log.info("Task-worker [{}] started.", Thread.currentThread().getName());

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

  private void doStop() {
    ExecutorService toShutdown = executor;

    executor = null;
    running = false;

    if (toShutdown == null) {
      log.info("ExecutorService is already null.");
      return;
    }

    log.info("Scheduler stopping");
    toShutdown.shutdownNow();
  }
}
