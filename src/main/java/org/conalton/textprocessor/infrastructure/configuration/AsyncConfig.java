package org.conalton.textprocessor.infrastructure.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;

@Configuration
@EnableAsync
@EnableConfigurationProperties(AsyncConfig.AsyncConfigProperties.class)
public class AsyncConfig {

  @Bean(name = AsyncJobExecutor.BackgroundJob)
  public Executor backgroundJobExecutor() {
    ThreadFactory factory = Thread.ofVirtual().name("App-Worker-", 0).factory();
    return new SimpleAsyncTaskExecutor(factory);
  }

  public static class AsyncJobExecutor {
    public static final String BackgroundJob = "backgroundJob";
  }

  @Validated
  @ConfigurationProperties(prefix = "app.async.job-runner")
  public static class AsyncConfigProperties {
    @Min(500)
    @Max(5000)
    private long restartDelayMs;

    public long getRestartDelayMs() {
      return restartDelayMs;
    }

    public void setRestartDelayMs(long restartDelayMs) {
      this.restartDelayMs = restartDelayMs;
    }
  }
}
