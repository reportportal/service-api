package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.PipelineStageTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestCaseRelatedEntityResolverTest {

  private static final Long PROJECT_ID = 1L;

  @Mock
  private TmsTestCaseService tmsTestCaseService;
  @Mock
  private PipelineStageTestCaseRepository stageTestCaseRepository;

  @InjectMocks
  private TestCaseRelatedEntityResolver resolver;

  private Pipeline pipeline;
  private PipelineStage stage;

  @BeforeEach
  void setUp() {
    pipeline = new Pipeline();
    stage = new PipelineStage();
    stage.setId(30L);
  }

  private static TmsTestCase testCase(String displayId) {
    var testCase = new TmsTestCase();
    testCase.setDisplayId(displayId);
    return testCase;
  }

  @Test
  void entityType_ShouldBeTestCase() {
    assertEquals("TEST_CASE", resolver.entityType());
  }

  @Test
  @SuppressWarnings("unchecked")
  void resolve_ShouldReplaceLinksAndReturnTheDisplayIdsThatDoNotExist() {
    when(tmsTestCaseService.getEntitiesByDisplayIds(eq(PROJECT_ID), anyCollection()))
        .thenReturn(List.of(testCase("TC-1")));

    var notFound = resolver.resolve(PROJECT_ID, pipeline, stage, List.of("TC-1", "TC-404"));

    assertEquals(List.of("TC-404"), notFound);
    verify(stageTestCaseRepository).deleteByStage_Id(30L);
    var saved = ArgumentCaptor.forClass(List.class);
    verify(stageTestCaseRepository).saveAll(saved.capture());
    var links = (List<PipelineStageTestCase>) saved.getValue();
    assertEquals(1, links.size());
    assertEquals("TC-1", links.get(0).getTestCase().getDisplayId());
    assertEquals(stage, links.get(0).getStage());
  }

  @Test
  void resolve_WithNoDisplayIds_ShouldOnlyClearTheStagesLinks() {
    var notFound = resolver.resolve(PROJECT_ID, pipeline, stage, List.of());

    assertTrue(notFound.isEmpty());
    verify(stageTestCaseRepository).deleteByStage_Id(30L);
    verify(tmsTestCaseService, never()).getEntitiesByDisplayIds(any(), anyCollection());
  }
}
