package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageContributor;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixPushRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Labels the iteration of a fix round, so that it is not taken for a plain re-run in the list.
 * <p>
 * Example: iteration #5 is the answer to round 1 of TC103 (it carries {@code fixPushId = 7}). This contributor
 * owns no stage; it adds the iteration attribute {@code fixRound = "TC103 · round 1"} that the list shows next to
 * the other facts of the iteration, and a Compare against #4 can tell the two apart.
 */
@Component
@RequiredArgsConstructor
public class FixRoundIterationContributor implements PipelineStageContributor {

  static final String FIX_ROUND_ATTRIBUTE_KEY = "fixRound";

  private final TmsTestCaseFixPushRepository fixPushRepository;
  private final TmsTestCaseRepository testCaseRepository;

  @Override
  public boolean supports(Pipeline pipeline) {
    return TestCaseReviewContributor.PIPELINE_TYPE.equals(pipeline.getType());
  }

  @Override
  public List<PipelineStage> contribute(PipelineIteration iteration, List<PipelineStage> stages) {
    return List.of();
  }

  @Override
  public List<PipelineAttribute> iterationAttributes(PipelineIteration iteration, List<PipelineStage> stages) {
    return FixRoundIterationSupport.fixPushId(iteration)
        .flatMap(fixPushRepository::findById)
        .map(push -> {
          var testCaseName = testCaseRepository.findById(push.getTestCaseId())
              .map(TmsTestCase::getDisplayId)
              .orElse("Test Case " + push.getTestCaseId());
          return List.of(PipelineAttribute.builder().key(FIX_ROUND_ATTRIBUTE_KEY).label("Fix round")
              .value(testCaseName + " · round " + push.getRoundNumber())
              .type(AttributeType.META).valueType(AttributeValueType.TEXT).build());
        })
        .orElse(List.of());
  }
}
