package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageReportedHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Ends a fix round as failed when the iteration the agent reported for it fails.
 * <p>
 * Example: the agent reported iteration #5 for round 1 of TC103 (attribute {@code fixRoundId = 7}) and its Fix
 * stage fails. The round ends as failed with the reason "Stage Fix failed", the comments of TC103 return to "not
 * sent" and the editor can push again. A failed Grade stage does not count: it does not fail the iteration, and
 * the round still ends with the fixed version the agent sends ("Fixed - Grade failed").
 */
@Component
@RequiredArgsConstructor
public class FixRoundOnStageFailed implements PipelineStageReportedHandler {

  private final TmsTestCaseFixRoundService fixRoundService;

  @Override
  public List<PipelineAttribute> onStageReported(StageReport report) {
    var stage = report.stage();
    if (!stage.getStatusCategory().endedUnsuccessfully() || !stage.isAffectsIterationStatus()) {
      return List.of();
    }
    FixRoundIterationSupport.fixRoundId(stage.getIteration())
        .ifPresent(roundId -> fixRoundService.failRound(roundId, "Stage '" + stage.getName() + "' " + ending(stage.getStatusCategory())));
    return List.of();
  }

  /**
   * How the stage ended, for the reason the round is given. Example: "failed", "timed out", "was interrupted".
   */
  private static String ending(StatusCategory category) {
    return switch (category) {
      case TIMED_OUT -> "timed out";
      case INTERRUPTED -> "was interrupted";
      default -> "failed";
    };
  }
}
