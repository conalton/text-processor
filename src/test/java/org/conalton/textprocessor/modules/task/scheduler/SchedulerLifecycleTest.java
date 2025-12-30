package org.conalton.textprocessor.modules.task.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.concurrent.*;
import org.conalton.textprocessor.modules.task.worker.config.WorkerProperties;
import org.conalton.textprocessor.modules.task.worker.scheduler.Scheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@ActiveProfiles("test")
@SpringJUnitConfig(
    classes = SchedulerLifecycleTest.SchedulerTestConfig.class,
    initializers = ConfigDataApplicationContextInitializer.class)
class SchedulerLifecycleTest {

  private SchedulerTestConfig.TestScheduler scheduler;

  @Autowired private WorkerProperties workerProps;

  @BeforeEach
  void setUp() {
    scheduler = new SchedulerTestConfig.TestScheduler(workerProps);
  }

  @AfterEach
  void tearDown() {
    scheduler.stop();
    scheduler = null;
  }

  @Test
  void startStopSequence() {
    scheduler.start();
    assertThat(scheduler.isRunning()).isTrue();

    scheduler.stop();
    assertThat(scheduler.isRunning()).isFalse();
  }

  @Test
  void restartSeveralTimes() {
    for (int i = 0; i < 3; i++) {
      scheduler.start();
      assertThat(scheduler.isRunning()).isTrue();

      scheduler.stop();
      assertThat(scheduler.isRunning()).isFalse();
    }
  }

  @Test
  void concurrentStartStop() throws Exception {
    CyclicBarrier barrier = new CyclicBarrier(2);
    CountDownLatch done = new CountDownLatch(2);

    Runnable startTask =
        () -> {
          await(barrier);
          assertThatCode(() -> scheduler.start()).doesNotThrowAnyException();
          done.countDown();
        };

    Runnable stopTask =
        () -> {
          await(barrier);
          assertThatCode(() -> scheduler.stop()).doesNotThrowAnyException();
          done.countDown();
        };

    var executor = Executors.newFixedThreadPool(2);

    Future<?> startTaskF = executor.submit(startTask);
    Future<?> stopTaskF = executor.submit(stopTask);

    try {
      assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();

      startTaskF.get(5, TimeUnit.SECONDS);
      stopTaskF.get(5, TimeUnit.SECONDS);

    } finally {
      executor.shutdownNow();
    }

    assertThatCode(() -> scheduler.stop()).doesNotThrowAnyException();
  }

  @Test
  void stopWithoutStart() {
    assertThatCode(() -> scheduler.stop()).doesNotThrowAnyException();
    assertThat(scheduler.isRunning()).isFalse();
  }

  @Test
  void workersActuallyStartAndDoWork() throws InterruptedException {
    CountDownLatch latch = scheduler.getWorkerLatch();
    scheduler.start();

    long waitTimeout =
        (workerProps.getStartDelayRangeMsMax() + workerProps.getLoopDelayRangeMsMax()) * 4L;

    assertThat(latch.await(waitTimeout, TimeUnit.MILLISECONDS))
        .as("at least one worker should execute runWorker()")
        .isTrue();
  }

  private static void await(CyclicBarrier barrier) {
    try {
      barrier.await();
    } catch (Exception ignored) {
    }
  }

  @TestConfiguration
  @EnableConfigurationProperties(WorkerProperties.class)
  static class SchedulerTestConfig {

    static class TestScheduler extends Scheduler {

      private final CountDownLatch workerLatch = new CountDownLatch(1);

      TestScheduler(WorkerProperties props) {
        super(props);
      }

      CountDownLatch getWorkerLatch() {
        return workerLatch;
      }

      @Override
      protected void runWorker(int delayMin, int delayMax) {
        workerLatch.countDown();
        super.runWorker(delayMin, delayMax);
      }
    }
  }
}
