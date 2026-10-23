package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixPushRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Ends fix rounds the agent never answered, so the comments of the case do not stay "sent" forever.
 * <p>
 * Example: with the default timeout of 30 minutes, a round pushed at 10:00 that has no fixed version and no
 * failed iteration by 10:30 ends as failed ("The agent did not answer in 30 minutes"); its comments return to
 * "not sent" and the editor can push again or discard them. The timeout is set with
 * {@code rp.tms.fix-push.timeout}, how often it is checked with {@code rp.tms.fix-push.check-interval}.
 */
@Slf4j
@Component
public class TmsFixPushTimeoutJob {

  private final TmsTestCaseFixPushRepository fixPushRepository;
  private final TmsTestCaseFixRoundService fixRoundService;
  private final Duration timeout;

  public TmsFixPushTimeoutJob(TmsTestCaseFixPushRepository fixPushRepository,
      TmsTestCaseFixRoundService fixRoundService,
      @Value("${rp.tms.fix-push.timeout:PT30M}") Duration timeout) {
    this.fixPushRepository = fixPushRepository;
    this.fixRoundService = fixRoundService;
    this.timeout = timeout;
  }

  @Scheduled(fixedDelayString = "${rp.tms.fix-push.check-interval:PT1M}",
      initialDelayString = "${rp.tms.fix-push.check-interval:PT1M}")
  public void failUnansweredRounds() {
    failUnansweredRounds(Instant.now());
  }

  /**
   * Ends every pending round that was pushed more than the timeout before {@code now}.
   *
   * @return how many rounds were ended
   */
  int failUnansweredRounds(Instant now) {
    var overdue = fixPushRepository.findByStatusAndRequestedAtBefore(TmsFixPushStatus.PENDING, now.minus(timeout));
    for (var push : overdue) {
      log.warn("Fix round {} of test case {} got no answer in {}; ending it as failed", push.getId(),
          push.getTestCaseId(), timeout);
      fixRoundService.failRound(push.getId(), "The agent did not answer in " + timeout.toMinutes() + " minutes");
    }
    return overdue.size();
  }
}
