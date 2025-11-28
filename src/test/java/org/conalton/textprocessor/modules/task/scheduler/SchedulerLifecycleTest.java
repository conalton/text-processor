package org.conalton.textprocessor.modules.task.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.conalton.textprocessor.modules.task.worker.config.WorkerProperties;
import org.conalton.textprocessor.modules.task.worker.scheduler.Scheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class SchedulerLifecycleTest {

  @Autowired private CountDownLatch workerStartedLatch;

  @Autowired private SchedulerTestConfig.TestScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler.resetWorkerLatch(1);
  }

  @AfterEach
  void tearDown() {
    scheduler.stop();
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
    try {
      executor.submit(startTask);
      executor.submit(stopTask);
      assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
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

    assertThat(latch.await(2, TimeUnit.SECONDS))
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
  static class SchedulerTestConfig {

    @Bean
    WorkerProperties workerProperties() {
      WorkerProperties props = new WorkerProperties();
      props.setNumWorkers(2);
      props.setStartDelayRangeMsMin(10);
      props.setStartDelayRangeMsMax(20);
      props.setLoopDelayRangeMsMin(50);
      props.setLoopDelayRangeMsMax(80);
      props.setShutdownDelayMs(500);
      return props;
    }

    @Bean
    CountDownLatch workerStartedLatch(WorkerProperties props) {
      return new CountDownLatch(1);
    }

    @Bean
    SchedulerTestConfig.TestScheduler scheduler(WorkerProperties props) {
      return new TestScheduler(props);
    }

    static class TestScheduler extends Scheduler {

      private volatile CountDownLatch workerLatch = new CountDownLatch(0);

      TestScheduler(WorkerProperties props) {
        super(props);
      }

      void resetWorkerLatch(int count) {
        this.workerLatch = new CountDownLatch(count);
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
