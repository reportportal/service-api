package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class PipelineIterationServiceImplTest {

  @Mock
  private PipelineRepository pipelineRepository;
  @Mock
  private PipelineIterationRepository pipelineIterationRepository;
  @Mock
  private PipelineStageRepository pipelineStageRepository;
  @Mock
  private PipelineStageTestCaseRepository pipelineStageTestCaseRepository;
  @Mock
  private TmsTestCaseService tmsTestCaseService;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private PipelineMapper pipelineMapper;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private PipelineIterationServiceImpl sut;

  private Long projectId;
  private Long userId;
  private Pipeline pipeline;
  private Project project;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    userId = 10L;

    project = new Project();
    project.setId(projectId);

    pipeline = new Pipeline();
    pipeline.setId(2L);
    pipeline.setProject(project);
    pipeline.setName("factory-tc-gen");
    pipeline.setAutoReadyEnabled(false);
  }

  private PipelineStageRQ stageRQ(String stageKey, PipelineRunStatus status, List<String> testCaseIds) {
    return PipelineStageRQ.builder()
        .stageKey(stageKey)
        .sequence(1)
        .status(status)
        .testCaseIds(testCaseIds)
        .build();
  }

  private PipelineStage stageEntity(PipelineRunStatus status) {
    var stage = new PipelineStage();
    stage.setStageKey("stage-gen-tc");
    stage.setSequence(1);
    stage.setStatus(status);
    return stage;
  }

  private void stubDetailRendering() {
    when(pipelineStageRepository.findByIterationIdOrderBySequenceAsc(any())).thenReturn(List.of());
    when(pipelineStageTestCaseRepository.findByStage_Iteration_Id(any())).thenReturn(List.of());
    when(pipelineMapper.toIterationDetailRS(any(), any()))
        .thenReturn(PipelineIterationDetailRS.builder().build());
  }

  @Test
  void ingestIteration_WhenRerunTrueWithoutRerunOfIterationId_ShouldThrowBadRequest() {
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen").rerun(true).stages(List.of()).build();

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));
    verifyNoInteractions(pipelineRepository);
  }

  @Test
  void ingestIteration_NewPipelineFirstIteration_ShouldCreatePipelineAndAssignIterationNumberOne() {
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, null))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.empty());
    when(projectRepository.getReferenceById(projectId)).thenReturn(project);
    when(pipelineRepository.save(any())).thenReturn(pipeline);
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    stubDetailRendering();

    var iterationCaptor = ArgumentCaptor.forClass(PipelineIteration.class);
    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineIterationRepository).save(iterationCaptor.capture());
    assertEquals(1, iterationCaptor.getValue().getIterationNumber());
    assertEquals(userId, iterationCaptor.getValue().getCreatedBy());
    assertEquals(PipelineRunStatus.PASSED, iterationCaptor.getValue().getStatus());
  }

  @Test
  void ingestIteration_ExistingPipelineWithPriorIterations_ShouldIncrementIterationNumber() {
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, null))).build();
    var previousIteration = new PipelineIteration();
    previousIteration.setIterationNumber(3);

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.of(previousIteration));
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    stubDetailRendering();

    var iterationCaptor = ArgumentCaptor.forClass(PipelineIteration.class);
    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineIterationRepository).save(iterationCaptor.capture());
    assertEquals(4, iterationCaptor.getValue().getIterationNumber());
    verify(projectRepository, never()).getReferenceById(any());
  }

  @Test
  void ingestIteration_WithAttributes_ShouldSetThemOnTheIteration() {
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .attributes(Map.of("skill", "create-test-cases@0.4"))
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, null))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    stubDetailRendering();

    var iterationCaptor = ArgumentCaptor.forClass(PipelineIteration.class);
    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineIterationRepository).save(iterationCaptor.capture());
    assertEquals(Map.of("skill", "create-test-cases@0.4"),
        iterationCaptor.getValue().getAttributes().getAttributes());
  }

  @Test
  void ingestIteration_WithoutAttributes_ShouldLeaveThemNull() {
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, null))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    stubDetailRendering();

    var iterationCaptor = ArgumentCaptor.forClass(PipelineIteration.class);
    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineIterationRepository).save(iterationCaptor.capture());
    assertEquals(null, iterationCaptor.getValue().getAttributes());
  }

  @Test
  void ingestIteration_WithMixedStageStatuses_ShouldAggregateFailedOverEverythingElse() {
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(
            stageRQ("stage-a", PipelineRunStatus.PASSED, null),
            stageRQ("stage-b", PipelineRunStatus.NEEDS_HUMAN, null),
            stageRQ("stage-c", PipelineRunStatus.FAILED, null)))
        .build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any()))
        .thenReturn(stageEntity(PipelineRunStatus.PASSED),
            stageEntity(PipelineRunStatus.NEEDS_HUMAN),
            stageEntity(PipelineRunStatus.FAILED));
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    stubDetailRendering();

    var iterationCaptor = ArgumentCaptor.forClass(PipelineIteration.class);
    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineIterationRepository).save(iterationCaptor.capture());
    assertEquals(PipelineRunStatus.FAILED, iterationCaptor.getValue().getStatus());
  }

  @Test
  void ingestIteration_WithNestedStage_ShouldAssignParentStageIdAndPath() {
    var parentStage = new PipelineStage();
    parentStage.setStageKey("parent");
    parentStage.setSequence(1);
    parentStage.setStatus(PipelineRunStatus.PASSED);
    parentStage.setId(500L);
    var childStage = new PipelineStage();
    childStage.setStageKey("child");
    childStage.setSequence(1);
    childStage.setStatus(PipelineRunStatus.PASSED);
    childStage.setId(501L);

    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(
            PipelineStageRQ.builder().stageKey("parent").sequence(1).status(PipelineRunStatus.PASSED).build(),
            PipelineStageRQ.builder().stageKey("child").sequence(1).status(PipelineRunStatus.PASSED)
                .parentStageKey("parent").build()))
        .build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(parentStage, childStage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    stubDetailRendering();

    sut.createPipelineIteration(projectId, userId, rq);

    assertEquals("500", parentStage.getPath());
    assertEquals(null, parentStage.getParentStageId());
    assertEquals("500.501", childStage.getPath());
    assertEquals(500L, childStage.getParentStageId());
  }

  @Test
  void ingestIteration_WithUnknownParentStageKey_ShouldThrowBadRequest() {
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(PipelineStageRQ.builder().stageKey("child").sequence(1)
            .status(PipelineRunStatus.PASSED).parentStageKey("missing-parent").build()))
        .build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));
  }

  @Test
  void ingestIteration_Rerun_ShouldReuseExistingIterationAndDeleteOldStages() {
    var existingIteration = new PipelineIteration();
    existingIteration.setId(99L);
    existingIteration.setPipeline(pipeline);
    existingIteration.setIterationNumber(2);
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .rerun(true).rerunOfIterationId(99L)
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, null))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findById(99L)).thenReturn(Optional.of(existingIteration));
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    stubDetailRendering();

    var iterationCaptor = ArgumentCaptor.forClass(PipelineIteration.class);
    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineStageRepository).deleteByIterationId(99L);
    verify(pipelineIterationRepository).save(iterationCaptor.capture());
    assertEquals(2, iterationCaptor.getValue().getIterationNumber());
    assertEquals(true, iterationCaptor.getValue().isRerun());
    assertEquals(null, iterationCaptor.getValue().getRerunOfIterationId());
    verify(pipelineRepository, never()).findByIdForUpdate(any());
  }

  @Test
  void ingestIteration_RerunOfIterationBelongingToDifferentPipeline_ShouldThrowNotFound() {
    var otherPipeline = new Pipeline();
    otherPipeline.setId(999L);
    var existingIteration = new PipelineIteration();
    existingIteration.setId(99L);
    existingIteration.setPipeline(otherPipeline);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .rerun(true).rerunOfIterationId(99L).stages(List.of()).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findById(99L)).thenReturn(Optional.of(existingIteration));

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));
    verify(pipelineStageRepository, never()).deleteByIterationId(any());
  }

  @Test
  void ingestIteration_WithUnresolvedDisplayId_ShouldSkipLinkingSilently() {
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, List.of("TC-404")))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    when(tmsTestCaseService.getEntitiesByDisplayIds(eq(projectId), any())).thenReturn(List.of());
    stubDetailRendering();

    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineStageTestCaseRepository, never()).saveAll(any());
    verify(tmsTestCaseService, never()).applyAutoReadyBatch(any(), any(), anyInt());
  }

  @Test
  void ingestIteration_WithResolvedDisplayIdAndAutoReadyDisabled_ShouldLinkButNotApplyAutoReady() {
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var testCase = new TmsTestCase();
    testCase.setId(5L);
    testCase.setDisplayId("TC1");
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, List.of("TC1")))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    when(tmsTestCaseService.getEntitiesByDisplayIds(eq(projectId), any())).thenReturn(List.of(testCase));
    stubDetailRendering();

    sut.createPipelineIteration(projectId, userId, rq);

    verify(pipelineStageTestCaseRepository).saveAll(any());
    verify(tmsTestCaseService, never()).applyAutoReadyBatch(any(), any(), anyInt());
  }

  @Test
  void ingestIteration_WithAutoReadyEnabledAndThreshold_ShouldApplyAutoReadyForLinkedTestCase() {
    pipeline.setAutoReadyEnabled(true);
    pipeline.setAutoReadyThreshold(80);
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var testCase = new TmsTestCase();
    testCase.setId(5L);
    testCase.setDisplayId("TC1");
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, List.of("TC1")))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    when(tmsTestCaseService.getEntitiesByDisplayIds(eq(projectId), any())).thenReturn(List.of(testCase));
    stubDetailRendering();

    sut.createPipelineIteration(projectId, userId, rq);

    verify(tmsTestCaseService).applyAutoReadyBatch(eq(projectId),
        argThat(testCases -> testCases.size() == 1 && testCases.contains(testCase)), eq(80));
  }

  @Test
  void ingestIteration_WithAutoReadyEnabledButNoThreshold_ShouldNotApplyAutoReady() {
    pipeline.setAutoReadyEnabled(true);
    pipeline.setAutoReadyThreshold(null);
    var stage = stageEntity(PipelineRunStatus.PASSED);
    var testCase = new TmsTestCase();
    testCase.setId(5L);
    testCase.setDisplayId("TC1");
    var rq = PipelineIterationRQ.builder().pipelineName("factory-tc-gen")
        .stages(List.of(stageRQ("stage-gen-tc", PipelineRunStatus.PASSED, List.of("TC1")))).build();

    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toStageEntity(any())).thenReturn(stage);
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    when(tmsTestCaseService.getEntitiesByDisplayIds(eq(projectId), any())).thenReturn(List.of(testCase));
    stubDetailRendering();

    sut.createPipelineIteration(projectId, userId, rq);

    verify(tmsTestCaseService, never()).applyAutoReadyBatch(any(), any(), anyInt());
  }

  @Test
  void listIterations_ShouldReturnPagedSummaries() {
    var pipelineId = pipeline.getId();
    var offsetRequest = new OffsetRequest(0, 50);
    var iteration = new PipelineIteration();
    iteration.setId(7L);
    var stage = stageEntity(PipelineRunStatus.PASSED);
    stage.setIteration(iteration);
    var stageSummary = PipelineStageSummaryRS.builder().id(50L).build();
    var page = new PageImpl<PipelineIteration>(List.of(iteration));
    var summary = PipelineIterationSummaryRS.builder().id(7L).build();

    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findByPipelineId(pipelineId, offsetRequest)).thenReturn(page);
    when(pipelineStageRepository.findByIterationIdInOrderByIterationIdAscSequenceAsc(List.of(7L)))
        .thenReturn(List.of(stage));
    when(pipelineMapper.toStageSummaryRS(stage)).thenReturn(stageSummary);
    when(pipelineMapper.toIterationSummaryRS(iteration, List.of(stageSummary))).thenReturn(summary);

    Page<PipelineIterationSummaryRS> result = sut.getPipelineIterations(projectId, pipelineId, offsetRequest);

    assertEquals(List.of(summary), result.getContent());
  }

  @Test
  void listIterations_WhenPipelineBelongsToDifferentProject_ShouldThrowNotFound() {
    var otherProject = new Project();
    otherProject.setId(999L);
    pipeline.setProject(otherProject);
    when(pipelineRepository.findById(pipeline.getId())).thenReturn(Optional.of(pipeline));

    assertThrows(ReportPortalException.class,
        () -> sut.getPipelineIterations(projectId, pipeline.getId(), new OffsetRequest(0, 50)));
    verifyNoInteractions(pipelineIterationRepository);
  }

  @Test
  void getIterationDetail_ShouldReturnDetailWithStages() {
    var iteration = new PipelineIteration();
    iteration.setId(7L);
    iteration.setPipeline(pipeline);
    var stage = stageEntity(PipelineRunStatus.PASSED);
    stage.setId(50L);
    var expected = PipelineIterationDetailRS.builder().id(7L).build();

    when(pipelineIterationRepository.findById(7L)).thenReturn(Optional.of(iteration));
    when(pipelineStageRepository.findByIterationIdOrderBySequenceAsc(7L)).thenReturn(List.of(stage));
    when(pipelineStageTestCaseRepository.findByStage_Iteration_Id(7L)).thenReturn(List.of());
    when(pipelineMapper.toStageRS(eq(stage), any())).thenReturn(PipelineStageRS.builder().id(50L).build());
    when(pipelineMapper.toIterationDetailRS(eq(iteration), any())).thenReturn(expected);

    var result = sut.getPipelineIteration(projectId, 7L);

    assertEquals(expected, result);
  }

  @Test
  void getIterationDetail_WhenIterationBelongsToDifferentProject_ShouldThrowNotFound() {
    var otherProject = new Project();
    otherProject.setId(999L);
    pipeline.setProject(otherProject);
    var iteration = new PipelineIteration();
    iteration.setId(7L);
    iteration.setPipeline(pipeline);

    when(pipelineIterationRepository.findById(7L)).thenReturn(Optional.of(iteration));

    assertThrows(ReportPortalException.class, () -> sut.getPipelineIteration(projectId, 7L));
  }

  @Test
  void compareIterations_SamePipeline_ShouldReturnCompareRS() {
    var current = new PipelineIteration();
    current.setId(7L);
    current.setPipeline(pipeline);
    var previous = new PipelineIteration();
    previous.setId(6L);
    previous.setPipeline(pipeline);
    var expected = PipelineCompareRS.builder().build();

    when(pipelineIterationRepository.findById(7L)).thenReturn(Optional.of(current));
    when(pipelineIterationRepository.findById(6L)).thenReturn(Optional.of(previous));
    when(pipelineStageRepository.findByIterationIdOrderBySequenceAsc(any())).thenReturn(List.of());
    when(pipelineStageTestCaseRepository.findByStage_Iteration_Id(any())).thenReturn(List.of());
    when(pipelineMapper.toIterationDetailRS(any(), any()))
        .thenReturn(PipelineIterationDetailRS.builder().id(7L).build(),
            PipelineIterationDetailRS.builder().id(6L).build());
    when(pipelineMapper.toCompareRS(any(), any())).thenReturn(expected);

    var result = sut.comparePipelineIterations(projectId, 7L, 6L);

    assertEquals(expected, result);
  }

  @Test
  void compareIterations_DifferentPipelines_ShouldThrowBadRequest() {
    var otherPipeline = new Pipeline();
    otherPipeline.setId(999L);
    otherPipeline.setProject(project);
    var current = new PipelineIteration();
    current.setId(7L);
    current.setPipeline(pipeline);
    var other = new PipelineIteration();
    other.setId(6L);
    other.setPipeline(otherPipeline);

    when(pipelineIterationRepository.findById(7L)).thenReturn(Optional.of(current));
    when(pipelineIterationRepository.findById(6L)).thenReturn(Optional.of(other));

    assertThrows(ReportPortalException.class, () -> sut.comparePipelineIterations(projectId, 7L, 6L));
    verifyNoInteractions(pipelineMapper);
  }
}
