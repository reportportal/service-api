package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.enums.AttributeRollup;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageReportedHandler.StageReport;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoReadyOnStagePassedTest {

  private static final Long PROJECT_ID = 1L;

  @Mock
  private PipelineStageTestCaseRepository stageTestCaseRepository;
  @Mock
  private TmsTestCaseService tmsTestCaseService;

  @InjectMocks
  private AutoReadyOnStagePassed handler;

  private Pipeline pipeline;
  private PipelineStage stage;

  @BeforeEach
  void setUp() {
    pipeline = new Pipeline();
    stage = new PipelineStage();
    stage.setId(30L);
  }

  private void autoReady(Object value) {
    pipeline.setSettings(new PipelineSettings(Map.of("autoReady", value)));
  }

  @Test
  void onStageReported_WhenTheStagePassedAndAutoReadyIsOn_ShouldPromoteAndAnswerWithTheCount() {
    autoReady(Map.of("enabled", true, "threshold", 90));
    var testCases = List.of(new TmsTestCase(), new TmsTestCase());
    when(stageTestCaseRepository.findTestCasesByStageId(30L)).thenReturn(testCases);
    when(tmsTestCaseService.applyAutoReadyBatch(PROJECT_ID, testCases, 90)).thenReturn(1);

    var attributes = handler.onStageReported(new StageReport(PROJECT_ID, pipeline, stage, true));

    assertEquals("autoReadyPromoted", attributes.get(0).getKey());
    assertEquals("1", attributes.get(0).getValue());
    assertEquals(AttributeRollup.SUM, attributes.get(0).getRollup());
    assertEquals("autoReadyThreshold", attributes.get(1).getKey());
    assertEquals("90", attributes.get(1).getValue());
  }

  @Test
  void onStageReported_WhenTheStageDidNotJustPass_ShouldDoNothing() {
    autoReady(Map.of("enabled", true, "threshold", 90));

    var attributes = handler.onStageReported(new StageReport(PROJECT_ID, pipeline, stage, false));

    assertTrue(attributes.isEmpty());
    verify(tmsTestCaseService, never()).applyAutoReadyBatch(any(), anyCollection(), anyInt());
  }

  @Test
  void onStageReported_WhenAutoReadyIsOffOrMissing_ShouldDoNothing() {
    assertTrue(handler.onStageReported(new StageReport(PROJECT_ID, pipeline, stage, true)).isEmpty());

    autoReady(Map.of("enabled", false, "threshold", 90));
    assertTrue(handler.onStageReported(new StageReport(PROJECT_ID, pipeline, stage, true)).isEmpty());

    verify(tmsTestCaseService, never()).applyAutoReadyBatch(eq(PROJECT_ID), anyCollection(), anyInt());
  }
}
