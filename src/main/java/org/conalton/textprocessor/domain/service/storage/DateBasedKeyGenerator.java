package org.conalton.textprocessor.domain.service.storage;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class DateBasedKeyGenerator {
  private final Clock clock;

  public DateBasedKeyGenerator() {
    this(Clock.systemUTC());
  }

  public DateBasedKeyGenerator(Clock clock) {
    this.clock = clock;
  }

  public String generateDateBasedKey(String id, String prefix) {
    LocalDate now = LocalDate.now(clock);

    return String.format(
        "%s/%d/%02d/%02d/%s", prefix, now.getYear(), now.getMonthValue(), now.getDayOfMonth(), id);
  }
}
