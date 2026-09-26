package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.enums.AttributeRollup;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageReportedHandler;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Marks the test cases of a stage as Ready on their own when the pipeline has Auto-Ready switched on and the
 * stage has just passed.
 * <p>
 * Example: the pipeline has {@code {"autoReady": {"enabled": true, "threshold": 90}}}. Its Upload stage
 * passes with test cases TC-1 (score 96), TC-2 (score 85) and TC-3 (score 88). TC-1 is promoted to Ready, and
 * the handler adds {@code autoReadyPromoted = 1} to the stage. The iteration sums that attribute over its
 * stages, so its totals can show "1 of 3 Auto-Ready promoted".
 */
@Component
@RequiredArgsConstructor
public class AutoReadyOnStagePassed implements PipelineStageReportedHandler {

  static final String PROMOTED_ATTRIBUTE_KEY = "autoReadyPromoted";
  static final String THRESHOLD_ATTRIBUTE_KEY = "autoReadyThreshold";

  private final PipelineStageTestCaseRepository stageTestCaseRepository;
  private final TmsTestCaseService tmsTestCaseService;

  @Override
  public List<PipelineAttribute> onStageReported(StageReport report) {
    if (!report.stagePassed()) {
      return List.of();
    }
    var autoReady = AutoReadySettings.of(report.pipeline()).filter(AutoReadySettings::active).orElse(null);
    if (autoReady == null) {
      return List.of();
    }
    var testCases = stageTestCaseRepository.findTestCasesByStageId(report.stage().getId());
    var promoted = tmsTestCaseService.applyAutoReadyBatch(report.projectId(), testCases, autoReady.threshold());
    return List.of(
        PipelineAttribute.builder().key(PROMOTED_ATTRIBUTE_KEY).label("Auto-Ready promoted")
            .value(String.valueOf(promoted)).type(AttributeType.MAIN).valueType(AttributeValueType.NUMBER)
            .rollup(AttributeRollup.SUM).build(),
        PipelineAttribute.builder().key(THRESHOLD_ATTRIBUTE_KEY).label("Auto-Ready threshold")
            .value(String.valueOf(autoReady.threshold())).type(AttributeType.HIDDEN)
            .valueType(AttributeValueType.NUMBER).build());
  }
}
