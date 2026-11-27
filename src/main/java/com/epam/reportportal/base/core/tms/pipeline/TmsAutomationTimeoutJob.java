package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationRefresher;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ends automation runs that the CI job never reported into, so the Test Case does not stay "In progress" forever
 * when the job dies before it sends its first stage.
 * <p>
 * Example: with the default timeout of 30 minutes, Automate of TC93 at 10:00 creates iteration #1 (Pending, no
 * stages) and starts the job. If the job fails at once and reports nothing, then by 10:30 the iteration gets the
 * stage "Automation" as "Timed out" ("The CI job did not report in 30 minutes") and ends as Timed out; TC93 becomes
 * "Failed" and the editor can press Automate again. A job that reported at least one stage is not touched, and if
 * its late report arrives it replaces the stage. The timeout is set with {@code rp.tms.automation.timeout}, how
 * often it is checked with {@code rp.tms.automation.check-interval}.
 */
@Slf4j
@Component
public class TmsAutomationTimeoutJob {

  static final String TIMEOUT_STAGE_KEY = "automation";

  private final PipelineIterationRepository iterationRepository;
  private final PipelineStageRepository stageRepository;
  private final PipelineIterationRefresher refresher;
  private final Duration timeout;

  public TmsAutomationTimeoutJob(PipelineIterationRepository iterationRepository,
      PipelineStageRepository stageRepository, PipelineIterationRefresher refresher,
      @Value("${rp.tms.automation.timeout:PT30M}") Duration timeout) {
    this.iterationRepository = iterationRepository;
    this.stageRepository = stageRepository;
    this.refresher = refresher;
    this.timeout = timeout;
  }

  @Scheduled(fixedDelayString = "${rp.tms.automation.check-interval:PT1M}",
      initialDelayString = "${rp.tms.automation.check-interval:PT1M}")
  @Transactional
  public void failSilentRuns() {
    failSilentRuns(Instant.now());
  }

  /**
   * Ends every automation iteration created more than the timeout before {@code now} that is still Pending and has
   * no stage.
   *
   * @return how many iterations were ended
   */
  @Transactional
  public int failSilentRuns(Instant now) {
    var ended = 0;
    var candidates = iterationRepository.findByPipelineTypeAndStatusCategoryAndCreatedAtBefore(
        TmsAutomationStartService.PIPELINE_TYPE, StatusCategory.PENDING, now.minus(timeout));
    for (var candidate : candidates) {
      // the job may have reported while we were reading: look again under the lock
      var iteration = iterationRepository.findByIdForUpdate(candidate.getId()).orElse(null);
      if (iteration == null || iteration.getStatusCategory() != StatusCategory.PENDING
          || !stageRepository.findByIterationIdOrderBySequenceAsc(iteration.getId()).isEmpty()) {
        continue;
      }
      log.warn("Automation iteration {} got no report from the CI job in {}; ending it as timed out",
          iteration.getId(), timeout);
      endAsTimedOut(iteration, now);
      ended++;
    }
    return ended;
  }

  private void endAsTimedOut(PipelineIteration iteration, Instant now) {
    var stage = new PipelineStage();
    stage.setIteration(iteration);
    stage.setStageKey(TIMEOUT_STAGE_KEY);
    stage.setName("Automation");
    stage.setSequence(1);
    stage.setOwnedBy(StageOwner.PIPELINE);
    stage.setStatusCategory(StatusCategory.TIMED_OUT);
    stage.setStatus(StatusCategory.TIMED_OUT.defaultLabel());
    stage.setStartedAt(iteration.getStartedAt());
    stage.setFinishedAt(now);
    stage.setMessage("The CI job did not report in " + timeout.toMinutes() + " minutes");
    var saved = stageRepository.save(stage);
    saved.setPath(String.valueOf(saved.getId()));
    stageRepository.save(saved);
    iteration.setFinishedAt(now);
    refresher.refresh(iteration);
  }
}
