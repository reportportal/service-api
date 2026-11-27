package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoReadyOnScoresRecordedTest {

  private static final Long PROJECT_ID = 1L;
  private static final Long TEST_CASE_ID = 42L;

  @Mock
  private PipelineStageTestCaseRepository stageTestCaseRepository;
  @Mock
  private TmsTestCaseService tmsTestCaseService;

  @InjectMocks
  private AutoReadyOnScoresRecorded sut;

  private static Pipeline pipelineWith(Object autoReady) {
    var pipeline = new Pipeline();
    if (autoReady != null) {
      pipeline.setSettings(new PipelineSettings(Map.of("autoReady", autoReady)));
    }
    return pipeline;
  }

  @Test
  void evaluate_WhenThePipelineHasAutoReadyOn_ShouldApplyItsThresholdAndAnswerWhetherTheCaseWasPromoted() {
    when(stageTestCaseRepository.findPipelinesByTestCaseIdOldestIterationFirst(TEST_CASE_ID))
        .thenReturn(List.of(pipelineWith(Map.of("enabled", true, "threshold", 95))));
    when(tmsTestCaseService.applyAutoReady(PROJECT_ID, TEST_CASE_ID, 95)).thenReturn(true);

    assertTrue(sut.evaluate(PROJECT_ID, TEST_CASE_ID));
  }

  @Test
  void evaluate_WhenTheScoreIsNotEnough_ShouldAnswerFalse() {
    when(stageTestCaseRepository.findPipelinesByTestCaseIdOldestIterationFirst(TEST_CASE_ID))
        .thenReturn(List.of(pipelineWith(Map.of("enabled", true, "threshold", 95))));
    when(tmsTestCaseService.applyAutoReady(PROJECT_ID, TEST_CASE_ID, 95)).thenReturn(false);

    assertFalse(sut.evaluate(PROJECT_ID, TEST_CASE_ID));
  }

  @Test
  void evaluate_ShouldUseThePipelineOfTheEarliestIteration() {
    var producer = pipelineWith(Map.of("enabled", true, "threshold", 90));
    var later = pipelineWith(Map.of("enabled", true, "threshold", 50));
    when(stageTestCaseRepository.findPipelinesByTestCaseIdOldestIterationFirst(TEST_CASE_ID))
        .thenReturn(List.of(producer, later));
    when(tmsTestCaseService.applyAutoReady(PROJECT_ID, TEST_CASE_ID, 90)).thenReturn(true);

    assertTrue(sut.evaluate(PROJECT_ID, TEST_CASE_ID));
    verify(tmsTestCaseService, never()).applyAutoReady(PROJECT_ID, TEST_CASE_ID, 50);
  }

  @Test
  void evaluate_WhenAutoReadyIsOffMissingOrWithoutThreshold_ShouldDoNothing() {
    when(stageTestCaseRepository.findPipelinesByTestCaseIdOldestIterationFirst(TEST_CASE_ID))
        .thenReturn(List.of(pipelineWith(Map.of("enabled", false, "threshold", 90))))
        .thenReturn(List.of(pipelineWith(null)))
        .thenReturn(List.of(pipelineWith(Map.of("enabled", true))));

    assertFalse(sut.evaluate(PROJECT_ID, TEST_CASE_ID));
    assertFalse(sut.evaluate(PROJECT_ID, TEST_CASE_ID));
    assertFalse(sut.evaluate(PROJECT_ID, TEST_CASE_ID));
    verify(tmsTestCaseService, never()).applyAutoReady(anyLong(), anyLong(), anyInt());
  }

  @Test
  void evaluate_WhenNoIterationListsTheCase_ShouldDoNothing() {
    when(stageTestCaseRepository.findPipelinesByTestCaseIdOldestIterationFirst(TEST_CASE_ID)).thenReturn(List.of());

    assertFalse(sut.evaluate(PROJECT_ID, TEST_CASE_ID));
    verify(tmsTestCaseService, never()).applyAutoReady(anyLong(), anyLong(), anyInt());
  }
}
