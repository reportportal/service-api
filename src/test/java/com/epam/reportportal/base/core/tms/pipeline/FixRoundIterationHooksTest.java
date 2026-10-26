package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageReportedHandler.StageReport;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixRound;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * How an iteration the agent reports is tied to the fix round it answers, and what happens when it fails or the
 * agent never answers.
 */
@ExtendWith(MockitoExtension.class)
class FixRoundIterationHooksTest {

  @Mock
  private TmsTestCaseFixRoundService fixRoundService;
  @Mock
  private TmsTestCaseFixRoundRepository fixRoundRepository;
  @Mock
  private TmsTestCaseRepository testCaseRepository;

  @InjectMocks
  private FixRoundOnStageFailed onStageFailed;
  @InjectMocks
  private FixRoundIterationContributor contributor;

  private static PipelineIteration iterationAnsweringRound(String roundId) {
    var iteration = new PipelineIteration();
    iteration.setAttributes(new PipelineAttributes(List.of(
        PipelineAttribute.builder().key("fixRoundId").value(roundId).type(AttributeType.NONE).build())));
    return iteration;
  }

  private static PipelineStage stage(PipelineIteration iteration, StatusCategory category, boolean affectsIteration) {
    var stage = new PipelineStage();
    stage.setName("Fix");
    stage.setIteration(iteration);
    stage.setStatusCategory(category);
    stage.setAffectsIterationStatus(affectsIteration);
    return stage;
  }

  @Test
  void onStageReported_WhenAStageOfAFixIterationFails_ShouldEndTheRoundAsFailed() {
    var stage = stage(iterationAnsweringRound("7"), StatusCategory.FAILED, true);

    onStageFailed.onStageReported(new StageReport(1L, new Pipeline(), stage, false));

    verify(fixRoundService).failRound(7L, "Stage 'Fix' failed");
  }

  @Test
  void onStageReported_WhenAStageThatDoesNotCountFails_ShouldLeaveTheRoundAlone() {
    var stage = stage(iterationAnsweringRound("7"), StatusCategory.FAILED, false);

    onStageFailed.onStageReported(new StageReport(1L, new Pipeline(), stage, false));

    verify(fixRoundService, never()).failRound(org.mockito.ArgumentMatchers.anyLong(), anyString());
  }

  @Test
  void onStageReported_WhenTheIterationAnswersNoRound_ShouldDoNothing() {
    var stage = stage(new PipelineIteration(), StatusCategory.FAILED, true);

    onStageFailed.onStageReported(new StageReport(1L, new Pipeline(), stage, false));

    verify(fixRoundService, never()).failRound(org.mockito.ArgumentMatchers.anyLong(), anyString());
  }

  @Test
  void onStageReported_WhenTheStageDidNotFail_ShouldDoNothing() {
    var stage = stage(iterationAnsweringRound("7"), StatusCategory.PASSED, true);

    onStageFailed.onStageReported(new StageReport(1L, new Pipeline(), stage, true));

    verify(fixRoundService, never()).failRound(org.mockito.ArgumentMatchers.anyLong(), anyString());
  }

  @Test
  void iterationAttributes_OfAFixIteration_ShouldLabelItWithTheCaseAndTheRound() {
    var round = new TmsTestCaseFixRound();
    round.setTestCaseId(103L);
    round.setRoundNumber(1);
    when(fixRoundRepository.findById(7L)).thenReturn(Optional.of(round));
    var testCase = new TmsTestCase();
    testCase.setDisplayId("TC103");
    when(testCaseRepository.findById(103L)).thenReturn(Optional.of(testCase));

    var attributes = contributor.iterationAttributes(iterationAnsweringRound("7"), List.of());

    assertEquals(1, attributes.size());
    assertEquals("fixRound", attributes.get(0).getKey());
    assertEquals("TC103 · round 1", attributes.get(0).getValue());
    assertEquals(AttributeType.META, attributes.get(0).getType());
  }

  @Test
  void iterationAttributes_OfAPlainIteration_ShouldAddNothing() {
    assertTrue(contributor.iterationAttributes(new PipelineIteration(), List.of()).isEmpty());
  }

  @Test
  void iterationAttributes_WithANonNumericRoundId_ShouldAddNothing() {
    assertTrue(contributor.iterationAttributes(iterationAnsweringRound("seven"), List.of()).isEmpty());
  }

  @Test
  void timeoutJob_ShouldEndTheRoundsOlderThanTheTimeoutAndLeaveTheRest() {
    var job = new TmsFixRoundTimeoutJob(fixRoundRepository, fixRoundService, Duration.ofMinutes(30));
    var now = Instant.parse("2026-10-20T10:30:00Z");
    var overdue = new TmsTestCaseFixRound();
    overdue.setId(7L);
    overdue.setTestCaseId(103L);
    when(fixRoundRepository.findByStatusAndRequestedAtBefore(
        com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus.PENDING,
        Instant.parse("2026-10-20T10:00:00Z"))).thenReturn(List.of(overdue));

    var ended = job.failUnansweredRounds(now);

    assertEquals(1, ended);
    verify(fixRoundService).failRound(7L, "The agent did not answer in 30 minutes");
  }
}
