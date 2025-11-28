package org.conalton.textprocessor.infrastructure.aws.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cloud.aws.sqs")
public class AwsSqsProperties {

  private int maxConcurrentMessages = 10;
  private int maxMessagesPerPoll = 10;
  private Duration maxDelayBetweenPolls = Duration.ofSeconds(1);

  public int getMaxConcurrentMessages() {
    return maxConcurrentMessages;
  }

  public void setMaxConcurrentMessages(int maxConcurrentMessages) {
    this.maxConcurrentMessages = maxConcurrentMessages;
  }

  public int getMaxMessagesPerPoll() {
    return maxMessagesPerPoll;
  }

  public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
    this.maxMessagesPerPoll = maxMessagesPerPoll;
  }

  public Duration getMaxDelayBetweenPolls() {
    return maxDelayBetweenPolls;
  }

  public void setMaxDelayBetweenPolls(Duration maxDelayBetweenPolls) {
    this.maxDelayBetweenPolls = maxDelayBetweenPolls;
  }
}
