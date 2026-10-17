package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

class PipelineIterationRefresherTest {

  private final PipelineIterationRepository iterationRepository = Mockito.mock(PipelineIterationRepository.class);
  private final PipelineStageRepository stageRepository = Mockito.mock(PipelineStageRepository.class);
  private final PipelineStageContributor contributor = Mockito.mock(PipelineStageContributor.class);

  private PipelineIterationRefresher refresher;
  private PipelineIteration iteration;

  @BeforeEach
  void setUp() {
    refresher = new PipelineIterationRefresher(iterationRepository, stageRepository,
        List.of(contributor));
    var pipeline = new Pipeline();
    pipeline.setType("T");
    iteration = new PipelineIteration();
    iteration.setId(7L);
    iteration.setPipeline(pipeline);
    when(iterationRepository.save(any(PipelineIteration.class))).thenAnswer(i -> i.getArgument(0));
    when(stageRepository.save(any(PipelineStage.class))).thenAnswer(i -> {
      var stage = (PipelineStage) i.getArgument(0);
      if (stage.getId() == null) {
        stage.setId(99L);
      }
      return stage;
    });
  }

  private static PipelineStage stage(long id, StatusCategory category, boolean counts) {
    var stage = new PipelineStage();
    stage.setId(id);
    stage.setStatus(category.defaultLabel());
    stage.setStatusCategory(category);
    stage.setAffectsIterationStatus(counts);
    return stage;
  }

  private void givenStages(PipelineStage... stages) {
    when(stageRepository.findByIterationIdOrderBySequenceAsc(7L)).thenReturn(List.of(stages));
    when(contributor.supports(any())).thenReturn(false);
  }

  @Test
  void refresh_WhenAStageThatDoesNotCountIsRunning_ShouldStillShowTheIterationAsRunning() {
    givenStages(stage(1, StatusCategory.PASSED, true), stage(2, StatusCategory.RUNNING, false),
        stage(3, StatusCategory.PENDING, true));

    refresher.refresh(iteration);

    assertEquals(StatusCategory.RUNNING, iteration.getStatusCategory());
  }

  @Test
  void refresh_WhenAStageThatDoesNotCountFails_ShouldNotFailTheIteration() {
    givenStages(stage(1, StatusCategory.PASSED, true), stage(2, StatusCategory.FAILED, false),
        stage(3, StatusCategory.PASSED, true));

    refresher.refresh(iteration);

    assertEquals(StatusCategory.PASSED, iteration.getStatusCategory());
  }

  @Test
  void refresh_ShouldDeriveWorstCategoryOfCountingStagesAndDefaultLabel() {
    givenStages(stage(1, StatusCategory.PASSED, true), stage(2, StatusCategory.FAILED, true),
        stage(3, StatusCategory.RUNNING, true));

    refresher.refresh(iteration);

    assertEquals(StatusCategory.FAILED, iteration.getStatusCategory());
    assertEquals("Failed", iteration.getStatus());
  }

  @Test
  void refresh_ShouldIgnoreStagesThatDoNotAffectTheIterationStatus() {
    givenStages(stage(1, StatusCategory.PASSED, true), stage(2, StatusCategory.FAILED, false));

    refresher.refresh(iteration);

    assertEquals(StatusCategory.PASSED, iteration.getStatusCategory());
  }

  @Test
  void refresh_WithAnUnknownStatusAndNothingElseCounting_ShouldBeUnknownAndOtherwiseIgnoreIt() {
    givenStages(stage(1, StatusCategory.UNKNOWN, true));
    refresher.refresh(iteration);
    assertEquals(StatusCategory.UNKNOWN, iteration.getStatusCategory());

    givenStages(stage(1, StatusCategory.UNKNOWN, true), stage(2, StatusCategory.RUNNING, true));
    refresher.refresh(iteration);
    assertEquals(StatusCategory.RUNNING, iteration.getStatusCategory());
  }

  @Test
  void refresh_WithoutStages_ShouldBePending() {
    givenStages();

    refresher.refresh(iteration);

    assertEquals(StatusCategory.PENDING, iteration.getStatusCategory());
    assertEquals("Pending", iteration.getStatus());
  }

  @Test
  void refresh_WithOnlyNonCountingStages_ShouldBePending() {
    givenStages(stage(1, StatusCategory.PENDING, false));

    refresher.refresh(iteration);

    assertEquals(StatusCategory.PENDING, iteration.getStatusCategory());
  }

  @Test
  void refresh_ShouldSaveContributedStagesGiveNewOnesTheirOwnIdAsPathAndUseTheContributorsLabel() {
    var passed = stage(1, StatusCategory.PASSED, true);
    when(stageRepository.findByIterationIdOrderBySequenceAsc(7L)).thenReturn(List.of(passed));
    when(contributor.supports(any())).thenReturn(true);
    var contributed = new PipelineStage();
    contributed.setStatus("In progress");
    contributed.setStatusCategory(StatusCategory.WAITING);
    contributed.setAffectsIterationStatus(true);
    when(contributor.contribute(any(), any())).thenReturn(List.of(contributed));
    when(contributor.iterationStatusLabel(StatusCategory.WAITING)).thenReturn(Optional.of("In review"));

    refresher.refresh(iteration);

    assertEquals("99", contributed.getPath());
    assertEquals(StatusCategory.WAITING, iteration.getStatusCategory());
    assertEquals("In review", iteration.getStatus());
  }

  @Test
  void refresh_ShouldAddIterationAttributesContributedByTheStages() {
    when(stageRepository.findByIterationIdOrderBySequenceAsc(7L))
        .thenReturn(List.of(stage(1, StatusCategory.PASSED, true)));
    when(contributor.supports(any())).thenReturn(true);
    when(contributor.contribute(any(), any())).thenReturn(List.of());
    when(contributor.iterationAttributes(any(), any())).thenReturn(List.of(
        com.epam.reportportal.base.core.aifactory.model.PipelineAttribute.builder().key("readyNow")
            .label("Ready now").value("2")
            .type(com.epam.reportportal.base.core.aifactory.enums.AttributeType.META)
            .valueType(com.epam.reportportal.base.core.aifactory.enums.AttributeValueType.NUMBER).build()));

    refresher.refresh(iteration);

    assertEquals("readyNow", iteration.getAttributes().getItems().get(0).getKey());
    assertEquals("2", iteration.getAttributes().getItems().get(0).getValue());
    assertEquals("2", iteration.getSearchText());
  }

  @Test
  void refreshIterations_ShouldRefreshEachIterationAndKeepGoingWhenOneFails() {
    when(iterationRepository.findByIdForUpdate(1L)).thenThrow(new IllegalStateException("boom"));
    when(iterationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(iteration));
    givenStages(stage(1, StatusCategory.PASSED, true));

    refresher.refreshIterations(List.of(1L, 7L));

    verify(iterationRepository).save(iteration);
  }
}
