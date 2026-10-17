package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.PipelineTestData;
import com.epam.reportportal.base.core.aifactory.dto.IterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationPatchRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineWarningRS;
import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageAttemptRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineExecutionData;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipelineIterationServiceImplTest {

  @Mock
  private PipelineRepository pipelineRepository;
  @Mock
  private PipelineIterationRepository pipelineIterationRepository;
  @Mock
  private PipelineStageRepository pipelineStageRepository;
  @Mock
  private PipelineStageAttemptRepository pipelineStageAttemptRepository;
  @Mock
  private PipelineRelatedEntityResolver testCaseResolver;
  @Mock
  private PipelineStageReportedHandler stageHandler;
  @Spy
  private List<PipelineStageReportedHandler> stageReportedHandlers = new ArrayList<>();
  @Spy
  private List<PipelineRelatedEntityResolver> relatedEntityResolvers = new ArrayList<>();
  @Mock
  private ProjectRepository projectRepository;
  @Spy
  private PipelineMapper pipelineMapper = new PipelineMapper();
  @Mock
  private PipelineViewAssembler viewAssembler;
  @Mock
  private PipelineIterationRefresher refresher;
  @Mock
  private PipelineComparator comparator;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private PipelineDocumentValidator documentValidator;

  private PipelineIterationServiceImpl sut;

  private Long projectId;
  private Long userId;
  private Pipeline pipeline;
  private Project project;
  private PipelineIterationDetailRS renderedDetail;
  private final AtomicLong ids = new AtomicLong(500);

  @BeforeEach
  void setUp() {
    projectId = 1L;
    userId = 10L;

    // built by hand: the two lists have the same type, so field injection could not tell them apart
    sut = new PipelineIterationServiceImpl(pipelineRepository, pipelineIterationRepository,
        pipelineStageRepository, pipelineStageAttemptRepository, relatedEntityResolvers, stageReportedHandlers,
        projectRepository, pipelineMapper, viewAssembler, refresher, comparator, eventPublisher,
        documentValidator);

    project = new Project();
    project.setId(projectId);

    pipeline = new Pipeline();
    pipeline.setId(2L);
    pipeline.setProject(project);
    pipeline.setName("factory-tc-gen");

    when(testCaseResolver.entityType()).thenReturn("TEST_CASE");
    when(testCaseResolver.resolve(any(), any(), any(), any()))
        .thenReturn(List.of());
    relatedEntityResolvers.add(testCaseResolver);
    when(stageHandler.onStageReported(any())).thenReturn(List.of());
    stageReportedHandlers.add(stageHandler);

    renderedDetail = PipelineIterationDetailRS.builder().build();
    when(pipelineRepository.findByProjectIdAndName(projectId, "factory-tc-gen")).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.findByIdForUpdate(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.empty());
    when(pipelineIterationRepository.save(any())).thenAnswer(inv -> {
      var iteration = (PipelineIteration) inv.getArgument(0);
      if (iteration.getId() == null) {
        iteration.setId(ids.incrementAndGet());
      }
      return iteration;
    });
    when(pipelineStageRepository.saveAll(any())).thenAnswer(inv -> {
      List<PipelineStage> stages = new ArrayList<>(inv.getArgument(0));
      stages.stream().filter(stage -> stage.getId() == null).forEach(stage -> stage.setId(ids.incrementAndGet()));
      return stages;
    });
    when(pipelineStageRepository.save(any())).thenAnswer(inv -> {
      var stage = (PipelineStage) inv.getArgument(0);
      if (stage.getId() == null) {
        stage.setId(ids.incrementAndGet());
      }
      return stage;
    });
    when(refresher.refresh(any())).thenAnswer(inv -> inv.getArgument(0));
    when(viewAssembler.detail(any())).thenReturn(renderedDetail);
  }

  private static PipelineStageRQ stageRQ(String stageKey, String status, List<PipelineSection.EntityReference> refs) {
    return PipelineStageRQ.builder().stageKey(stageKey).status(status)
        .sections(refs == null ? null : PipelineTestData.sectionLinking(refs)).build();
  }

  private PipelineIteration persistedIteration(long id) {
    var iteration = new PipelineIteration();
    iteration.setId(id);
    iteration.setPipeline(pipeline);
    iteration.setIterationNumber(2);
    iteration.setStatus("Running");
    iteration.setStatusCategory(StatusCategory.RUNNING);
    when(pipelineIterationRepository.findById(id)).thenReturn(Optional.of(iteration));
    when(pipelineIterationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(iteration));
    return iteration;
  }

  private PipelineStage persistedStage(long iterationId, String key, String status, StatusCategory category) {
    var stage = new PipelineStage();
    stage.setId(ids.incrementAndGet());
    stage.setStageKey(key);
    stage.setName(key);
    stage.setSequence(1);
    stage.setAttempt(1);
    stage.setStatus(status);
    stage.setStatusCategory(category);
    stage.setOwnedBy(StageOwner.PIPELINE);
    pipelineIterationRepository.findById(iterationId).ifPresent(stage::setIteration);
    when(pipelineStageRepository.findByIterationIdAndStageKey(iterationId, key)).thenReturn(Optional.of(stage));
    return stage;
  }

  // ------------------------------------------------------------------ create

  @Test
  void create_NewPipelineFirstIteration_ShouldCreatePipelineWithTypeAndNumberOne() {
    when(pipelineRepository.findByProjectIdAndName(projectId, "new-pipeline")).thenReturn(Optional.empty());
    when(projectRepository.getReferenceById(projectId)).thenReturn(project);
    var created = new Pipeline();
    created.setId(3L);
    created.setProject(project);
    created.setName("new-pipeline");
    when(pipelineRepository.save(any())).thenAnswer(inv -> {
      var saved = (Pipeline) inv.getArgument(0);
      saved.setId(3L);
      return saved;
    });
    when(pipelineRepository.findByIdForUpdate(3L)).thenAnswer(inv -> Optional.of(created));
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(3L))
        .thenReturn(Optional.empty());
    var rq = PipelineIterationRQ.builder().name("new-pipeline").type("TEST_CASE_GENERATION").iteration(IterationRQ.builder().stages(List.of(stageRQ("create", "Passed", null))).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    var pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
    verify(pipelineRepository, org.mockito.Mockito.atLeastOnce()).save(pipelineCaptor.capture());
    assertTrue(pipelineCaptor.getAllValues().stream().anyMatch(p -> "new-pipeline".equals(p.getName())));
    var iterationCaptor = ArgumentCaptor.forClass(PipelineIteration.class);
    verify(pipelineIterationRepository, org.mockito.Mockito.atLeastOnce()).save(iterationCaptor.capture());
    assertEquals(1, iterationCaptor.getAllValues().get(0).getIterationNumber());
    assertEquals(userId, iterationCaptor.getAllValues().get(0).getCreatedBy());
  }

  @Test
  void create_ExistingPipelineWithPriorIterations_ShouldIncrementIterationNumber() {
    var previous = new PipelineIteration();
    previous.setIterationNumber(3);
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId()))
        .thenReturn(Optional.of(previous));
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    var captor = ArgumentCaptor.forClass(PipelineIteration.class);
    verify(pipelineIterationRepository).save(captor.capture());
    assertEquals(4, captor.getValue().getIterationNumber());
    verify(projectRepository, never()).getReferenceById(any());
  }

  @Test
  void create_ShouldRecordPipelineTypeTheFirstTimeAndMergePipelineAttributes() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").type("TEST_CASE_GENERATION").attributes(List.of(PipelineAttribute.builder().key("repo").value("org/repo").build())).iteration(IterationRQ.builder().build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    assertEquals("TEST_CASE_GENERATION", pipeline.getType());
    assertEquals("repo", pipeline.getAttributes().getItems().get(0).getKey());
  }

  @Test
  void create_WithADifferentTypeThanRecorded_ShouldBeRejected() {
    pipeline.setType("TEST_AUTOMATION");
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").type("TEST_CASE_GENERATION").iteration(IterationRQ.builder().build()).build();

    var ex = assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));

    assertTrue(ex.getMessage().contains("TEST_AUTOMATION"));
    verify(pipelineIterationRepository, never()).save(any());
  }

  @Test
  void create_WithoutStages_ShouldBeAllowedAndRefreshTheDerivedStatus() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build();

    var result = sut.createPipelineIteration(projectId, userId, rq, null);

    assertFalse(result.replayed());
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).saveAll(List.of());
    verify(refresher).refresh(any(PipelineIteration.class));
  }

  @Test
  void create_WithAttributes_ShouldStoreNormalizedAttributesSearchTextAndIndex() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().attributes(List.of(
            PipelineAttribute.builder().key("requirement").value("US-TMS-EXP-002").build(),
            PipelineAttribute.builder().key("env").value("beta5").build())).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    var captor = ArgumentCaptor.forClass(PipelineIteration.class);
    verify(pipelineIterationRepository).save(captor.capture());
    var iteration = captor.getValue();
    assertEquals(2, iteration.getAttributes().getItems().size());
    assertEquals("us-tms-exp-002 beta5", iteration.getSearchText());
    assertTrue(iteration.getAttributeIndex().contains(PipelineAttribute.indexToken("env", "beta5")));
  }

  @Test
  void create_WithoutAttributes_ShouldLeaveThemNull() {
    sut.createPipelineIteration(projectId, userId, PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build());

    var captor = ArgumentCaptor.forClass(PipelineIteration.class);
    verify(pipelineIterationRepository).save(captor.capture());
    assertNull(captor.getValue().getAttributes());
    assertNull(captor.getValue().getSearchText());
  }

  @Test
  void create_WithStages_ShouldSaveThemWithAttemptOneAndSequenceFromPositionWhenAbsent() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ("create", "Passed", null), stageRQ("grade", "Running", null))).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PipelineStage>> captor = ArgumentCaptor.forClass(List.class);
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
    var stages = captor.getAllValues().get(0);
    assertEquals(List.of(1, 2), stages.stream().map(PipelineStage::getSequence).toList());
    assertEquals(List.of(1, 1), stages.stream().map(PipelineStage::getAttempt).toList());
    assertEquals(StatusCategory.PASSED, stages.get(0).getStatusCategory());
    assertEquals(StatusCategory.RUNNING, stages.get(1).getStatusCategory());
  }

  @Test
  void create_WithNestedStage_ShouldAssignParentStageIdAndPath() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(
            PipelineStageRQ.builder().stageKey("parent").status("Passed").build(),
            PipelineStageRQ.builder().stageKey("child").status("Passed").parentStageKey("parent").build())).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PipelineStage>> captor = ArgumentCaptor.forClass(List.class);
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
    var stages = captor.getAllValues().get(0);
    var parent = stages.get(0);
    var child = stages.get(1);
    assertEquals(String.valueOf(parent.getId()), parent.getPath());
    assertNull(parent.getParentStageId());
    assertEquals(parent.getId() + "." + child.getId(), child.getPath());
    assertEquals(parent.getId(), child.getParentStageId());
  }

  @Test
  void create_WithUnknownParentStageKey_ShouldThrowBadRequest() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(PipelineStageRQ.builder().stageKey("child").status("Passed")
            .parentStageKey("missing-parent").build())).build()).build();

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));
  }

  @Test
  void create_WithCycleInParentChain_ShouldThrowBadRequest() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(
            PipelineStageRQ.builder().stageKey("a").status("Passed").parentStageKey("b").build(),
            PipelineStageRQ.builder().stageKey("b").status("Passed").parentStageKey("a").build())).build()).build();

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));
  }

  @Test
  void create_WithDuplicateOrBlankStageKeys_ShouldThrowBadRequest() {
    var duplicate = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ("a", "Passed", null), stageRQ("a", "Passed", null))).build()).build();
    var blank = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ(" ", "Passed", null))).build()).build();

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, duplicate));
    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, blank));
  }

  @Test
  void create_WithOversizedReport_ShouldBeRejected() {
    var document = Map.<String, Object>of("blob", "x".repeat((int) PipelineIterationServiceImpl.MAX_REPORT_BYTES));
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(PipelineStageRQ.builder().stageKey("grade").status("Passed").document(document).build())).build()).build();

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));
    verify(pipelineRepository, never()).findByProjectIdAndName(any(), any());
  }

  @Test
  void create_RerunOfAnIterationOfTheSamePipeline_ShouldCreateNewIterationLinkedToTheSource() {
    var source = persistedIteration(99L);
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().rerunOfIterationId(99L).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    var captor = ArgumentCaptor.forClass(PipelineIteration.class);
    verify(pipelineIterationRepository).save(captor.capture());
    assertTrue(captor.getValue().isRerun());
    assertEquals(source.getId(), captor.getValue().getRerunOfIterationId());
    org.junit.jupiter.api.Assertions.assertNotEquals(source.getId(), captor.getValue().getId());
  }

  @Test
  void create_RerunOfAnIterationOfAnotherPipeline_ShouldThrowNotFound() {
    var other = new Pipeline();
    other.setId(77L);
    var source = new PipelineIteration();
    source.setId(99L);
    source.setPipeline(other);
    when(pipelineIterationRepository.findById(99L)).thenReturn(Optional.of(source));
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().rerunOfIterationId(99L).build()).build();

    var ex = assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq));

    assertTrue(ex.getMessage().contains("99"));
    verify(pipelineIterationRepository, never()).save(any());
  }

  @Test
  void create_ShouldPublishIngestedEventAfterRefreshing() {
    sut.createPipelineIteration(projectId, userId, PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build());

    verify(eventPublisher).publishEvent(any(com.epam.reportportal.base.core.aifactory.event.PipelineIterationIngestedEvent.class));
  }

  // ------------------------------------------------------------------ idempotency

  @Test
  void create_WithBlankOrTooLongIdempotencyKey_ShouldThrowBadRequest() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build();

    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq, " "));
    assertThrows(ReportPortalException.class, () -> sut.createPipelineIteration(projectId, userId, rq,
        "k".repeat(PipelineIterationServiceImpl.MAX_IDEMPOTENCY_KEY_LENGTH + 1)));
  }

  @Test
  void create_ReplayOfTheSameKeyAndBody_ShouldAnswerWithTheFirstIterationAndCreateNothing() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build();
    var previous = new PipelineIteration();
    previous.setId(321L);
    previous.setIdempotencyHash(IngestFingerprint.of(rq));
    when(pipelineIterationRepository.findByPipelineIdAndIdempotencyKey(pipeline.getId(), "key-1"))
        .thenReturn(Optional.of(previous));

    var result = sut.createPipelineIteration(projectId, userId, rq, "key-1");

    assertTrue(result.replayed());
    assertSame(renderedDetail, result.iteration());
    verify(pipelineIterationRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void create_SameKeyWithDifferentBody_ShouldThrowIdempotencyConflict() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build();
    var previous = new PipelineIteration();
    previous.setIdempotencyHash("another-hash");
    when(pipelineIterationRepository.findByPipelineIdAndIdempotencyKey(pipeline.getId(), "key-1"))
        .thenReturn(Optional.of(previous));

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.createPipelineIteration(projectId, userId, rq, "key-1"));

    assertEquals(ErrorType.IDEMPOTENCY_KEY_CONFLICT.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void create_WithNewKey_ShouldStoreKeyAndFingerprintOnTheIteration() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().build()).build();
    when(pipelineIterationRepository.findByPipelineIdAndIdempotencyKey(pipeline.getId(), "key-1"))
        .thenReturn(Optional.empty());

    sut.createPipelineIteration(projectId, userId, rq, "key-1");

    var captor = ArgumentCaptor.forClass(PipelineIteration.class);
    verify(pipelineIterationRepository).save(captor.capture());
    assertEquals("key-1", captor.getValue().getIdempotencyKey());
    assertEquals(IngestFingerprint.of(rq), captor.getValue().getIdempotencyHash());
  }

  // ------------------------------------------------------------------ refs

  private static List<PipelineSection.EntityReference> relatedTestCases(String... displayIds) {
    return java.util.Arrays.stream(displayIds)
        .map(id -> new PipelineSection.EntityReference("TEST_CASE", id)).toList();
  }

  @Test
  void create_WithStageRefs_ShouldHandThemToTheResolverAndWarnAboutTheUnresolvedOnes() {
    when(testCaseResolver.resolve(eq(projectId), any(), any(), eq(List.of("TC-1", "TC-404"))))
        .thenReturn(List.of("TC-404"));
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ("upload", "Passed", relatedTestCases("TC-1", "TC-404")))).build()).build();
    var detail = PipelineIterationDetailRS.builder().build();
    when(viewAssembler.detail(any())).thenReturn(detail);

    var result = sut.createPipelineIteration(projectId, userId, rq);

    var warnings = result.getWarnings();
    assertEquals(1, warnings.size());
    assertEquals(PipelineWarningRS.RELATED_ENTITY_NOT_FOUND, warnings.get(0).getCode());
    assertEquals("TEST_CASE", warnings.get(0).getEntityType());
    assertEquals(List.of("TC-404"), warnings.get(0).getEntityIds());
  }

  @Test
  void create_ShouldTellTheHandlersWhetherEachStagePassed() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ("create", "Passed", null), stageRQ("upload", "Running", null))).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    var reports = ArgumentCaptor.forClass(PipelineStageReportedHandler.StageReport.class);
    verify(stageHandler, org.mockito.Mockito.times(2)).onStageReported(reports.capture());
    assertEquals(true, reports.getAllValues().get(0).stagePassed());
    assertEquals(false, reports.getAllValues().get(1).stagePassed());
    assertSame(pipeline, reports.getAllValues().get(0).pipeline());
  }

  @Test
  void create_WithAHandlerAnswer_ShouldAddItsAttributesToTheStage() {
    var promoted = PipelineAttribute.builder().key("autoReadyPromoted").value("2").build();
    when(stageHandler.onStageReported(any())).thenReturn(List.of(promoted));
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ("upload", "Passed", null))).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    var saved = ArgumentCaptor.forClass(PipelineStage.class);
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).save(saved.capture());
    assertEquals("2", saved.getValue().getAttributes().getItems().stream()
        .filter(attribute -> "autoReadyPromoted".equals(attribute.getKey())).findFirst().orElseThrow().getValue());
  }

  @Test
  void create_WithRelatedEntitiesOfATypeNothingHandles_ShouldKeepThemWithoutWarning() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ("upload", "Passed", List.of(new PipelineSection.EntityReference("LAUNCH", "12"))))).build()).build();
    var detail = PipelineIterationDetailRS.builder().build();
    when(viewAssembler.detail(any())).thenReturn(detail);

    var result = sut.createPipelineIteration(projectId, userId, rq);

    assertNull(result.getWarnings());
    var saved = ArgumentCaptor.forClass(List.class);
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).saveAll(saved.capture());
    var stage = (PipelineStage) saved.getAllValues().get(0).get(0);
    assertEquals("LAUNCH", stage.getRelatedEntities().getItems().get(0).getEntityType());
    assertEquals("12", stage.getRelatedEntities().getItems().get(0).getEntityId());
  }

  @Test
  void create_WithoutRefs_ShouldNotCallTheResolvers() {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(stageRQ("upload", "Passed", null))).build()).build();

    sut.createPipelineIteration(projectId, userId, rq);

    verify(testCaseResolver, never()).resolve(any(), any(), any(), any());
  }

  // ------------------------------------------------------------------ report stage

  @Test
  void reportStage_ForANewStage_ShouldCreateItAfterTheExistingOnesWithRootPathAndRefresh() {
    var iteration = persistedIteration(10L);
    var create = new PipelineStage();
    create.setSequence(4);
    when(pipelineStageRepository.findByIterationIdOrderBySequenceAsc(10L)).thenReturn(List.of(create));

    var result = sut.reportStage(projectId, 10L, "grade", stageRQ(null, "Queued · waiting for runner", null));

    var captor = ArgumentCaptor.forClass(PipelineStage.class);
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
    var stage = captor.getValue();
    assertEquals("grade", stage.getStageKey());
    assertEquals(5, stage.getSequence());
    assertEquals(1, stage.getAttempt());
    assertEquals(String.valueOf(stage.getId()), stage.getPath());
    assertEquals(StatusCategory.UNKNOWN, stage.getStatusCategory());
    assertSame(iteration, stage.getIteration());
    verify(refresher).refresh(iteration);
    assertSame(renderedDetail, result);
  }

  @Test
  void reportStage_WithADocumentThatFailsItsSchema_ShouldFailTheStageAndNotKeepTheDocument() {
    persistedIteration(10L);
    when(pipelineStageRepository.findByIterationIdOrderBySequenceAsc(10L)).thenReturn(List.of());
    when(documentValidator.validate(org.mockito.ArgumentMatchers.eq("grade"), any()))
        .thenReturn(Optional.of("total_score is missing"));
    var rq = PipelineStageRQ.builder().status("Passed").documentSchemaName("grade")
        .document(java.util.Map.of("warnings", List.of())).build();

    sut.reportStage(projectId, 10L, "grade", rq);

    var captor = ArgumentCaptor.forClass(PipelineStage.class);
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
    var stage = captor.getValue();
    assertEquals(StatusCategory.FAILED, stage.getStatusCategory());
    assertEquals("Failed", stage.getStatus());
    org.junit.jupiter.api.Assertions.assertNull(stage.getDocument());
    assertEquals("total_score is missing", stage.getAttributes().getItems().stream()
        .filter(attribute -> "failureReason".equals(attribute.getKey()))
        .findFirst().orElseThrow().getValue());
  }

  @Test
  void reportStage_ForANewNestedStage_ShouldHangItUnderTheParentPath() {
    persistedIteration(10L);
    var parent = persistedStage(10L, "parent", "Running", StatusCategory.RUNNING);
    parent.setPath("42");

    sut.reportStage(projectId, 10L, "child",
        PipelineStageRQ.builder().status("Running").parentStageKey("parent").build());

    var captor = ArgumentCaptor.forClass(PipelineStage.class);
    verify(pipelineStageRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
    var child = captor.getValue();
    assertEquals(parent.getId(), child.getParentStageId());
    assertEquals("42." + child.getId(), child.getPath());
  }

  @Test
  void reportStage_WithUnknownParentKey_ShouldThrowBadRequest() {
    persistedIteration(10L);

    assertThrows(ReportPortalException.class, () -> sut.reportStage(projectId, 10L, "child",
        PipelineStageRQ.builder().status("Running").parentStageKey("nope").build()));
  }

  @Test
  void reportStage_ForExistingStageUnderADifferentParent_ShouldBeRejected() {
    persistedIteration(10L);
    persistedStage(10L, "other-parent", "Running", StatusCategory.RUNNING);
    var stage = persistedStage(10L, "child", "Running", StatusCategory.RUNNING);
    stage.setParentStageId(1L);

    assertThrows(ReportPortalException.class, () -> sut.reportStage(projectId, 10L, "child",
        PipelineStageRQ.builder().status("Passed").parentStageKey("other-parent").build()));
  }

  @Test
  void reportStage_FromTheSameJobAgain_ShouldUpdateTheCurrentAttemptInPlace() {
    var iteration = persistedIteration(10L);
    var stage = persistedStage(10L, "upload", "Running", StatusCategory.RUNNING);
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder().executorType("GITLAB_CI").externalRunId("77").build()));

    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Passed")
        .execution(PipelineExecution.builder().executorType("GITLAB_CI").externalRunId("77").build()).build());

    assertEquals(1, stage.getAttempt());
    assertEquals("Passed", stage.getStatus());
    assertEquals(StatusCategory.PASSED, stage.getStatusCategory());
    verify(pipelineStageAttemptRepository, never()).save(any());
    verify(refresher).refresh(iteration);
  }

  @Test
  void reportStage_OfAFinishedStageByAnotherJob_ShouldKeepTheFinishedAttemptAndStartANewOne() {
    persistedIteration(10L);
    var stage = persistedStage(10L, "upload", "Failed", StatusCategory.FAILED);
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder().executorType("GITLAB_CI").externalRunId("77").build()));

    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Running")
        .execution(PipelineExecution.builder().executorType("GITLAB_CI").externalRunId("78").build()).build());

    var captor = ArgumentCaptor.forClass(PipelineStageAttempt.class);
    verify(pipelineStageAttemptRepository).save(captor.capture());
    assertEquals(1, captor.getValue().getAttempt());
    assertEquals("Failed", captor.getValue().getStatus());
    assertEquals(2, stage.getAttempt());
    assertEquals(StatusCategory.RUNNING, stage.getStatusCategory());
  }

  @Test
  void reportStage_WithRelatedEntities_ShouldHandThemToTheResolver() {
    persistedIteration(10L);
    persistedStage(10L, "upload", "Running", StatusCategory.RUNNING);

    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Running")
        .sections(PipelineTestData.sectionLinking(relatedTestCases("TC-1"))).build());

    verify(testCaseResolver).resolve(eq(projectId), eq(pipeline), any(), eq(List.of("TC-1")));
  }

  @Test
  void reportStage_ShouldSignalAPassToTheHandlersOnlyWhenTheStageBecomesPassed() {
    persistedIteration(10L);
    persistedStage(10L, "upload", "Running", StatusCategory.RUNNING);

    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Running").build());
    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Passed").build());
    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Passed").build());

    var reports = ArgumentCaptor.forClass(PipelineStageReportedHandler.StageReport.class);
    verify(stageHandler, org.mockito.Mockito.times(3)).onStageReported(reports.capture());
    assertEquals(List.of(false, true, false),
        reports.getAllValues().stream().map(PipelineStageReportedHandler.StageReport::stagePassed).toList());
  }

  @Test
  void reportStage_WhenTheStageWasRelatedAndTheReportLinksNothing_ShouldLetTheResolverClearIt() {
    persistedIteration(10L);
    persistedStage(10L, "upload", "Running", StatusCategory.RUNNING).setRelatedEntities(
        new com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineRelatedEntities(
            relatedTestCases("TC-1")));

    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Running").build());

    verify(testCaseResolver).resolve(eq(projectId), eq(pipeline), any(), eq(List.of()));
  }

  @Test
  void reportStage_WithoutRefs_ShouldLeaveWhatTheStageReferencesAlone() {
    persistedIteration(10L);
    persistedStage(10L, "upload", "Running", StatusCategory.RUNNING);

    sut.reportStage(projectId, 10L, "upload", PipelineStageRQ.builder().status("Passed").build());

    verify(testCaseResolver, never()).resolve(any(), any(), any(), any());
  }

  @Test
  void reportStage_WithMismatchingKeyInBody_ShouldThrowBadRequest() {
    persistedIteration(10L);

    assertThrows(ReportPortalException.class,
        () -> sut.reportStage(projectId, 10L, "grade", PipelineStageRQ.builder().stageKey("other").status("x").build()));
  }

  @Test
  void reportStage_ForUnknownIteration_ShouldThrowNotFound() {
    when(pipelineIterationRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.reportStage(projectId, 10L, "grade", stageRQ(null, "Running", null)));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void reportStage_ForIterationOfAnotherProject_ShouldThrowNotFound() {
    persistedIteration(10L);

    assertThrows(ReportPortalException.class,
        () -> sut.reportStage(999L, 10L, "grade", stageRQ(null, "Running", null)));
  }

  @Test
  void reportStage_ForSystemManagedStage_ShouldBeRejected() {
    persistedIteration(10L);
    persistedStage(10L, "review", "In progress", StatusCategory.WAITING).setOwnedBy(StageOwner.REPORTPORTAL);

    assertThrows(ReportPortalException.class,
        () -> sut.reportStage(projectId, 10L, "review", stageRQ(null, "Done", null)));
  }

  @Test
  void patch_ShouldMergeAttributesRemoveKeysAndSetTimes() {
    var iteration = persistedIteration(10L);
    iteration.setAttributes(new PipelineAttributes(List.of(
        PipelineAttribute.builder().key("env").value("beta4").build(),
        PipelineAttribute.builder().key("jira").value("A-1").build())));
    var finished = java.time.Instant.parse("2026-01-01T10:00:00Z");

    sut.patchPipelineIteration(projectId, 10L, PipelineIterationPatchRQ.builder()
        .attributes(List.of(PipelineAttribute.builder().key("env").value("beta5").build(),
            PipelineAttribute.builder().key("model").value("auto").build()))
        .removeAttributes(List.of("jira"))
        .finishedAt(finished).build());

    assertEquals(List.of("env", "model"),
        iteration.getAttributes().getItems().stream().map(PipelineAttribute::getKey).toList());
    assertEquals("beta5", iteration.getAttributes().getItems().get(0).getValue());
    assertEquals("beta5 auto", iteration.getSearchText());
    assertEquals(finished, iteration.getFinishedAt());
    verify(refresher).refresh(iteration);
  }

  @Test
  void patch_WithNothingToChange_ShouldLeaveAttributesAlone() {
    var iteration = persistedIteration(10L);
    var attributes = new PipelineAttributes(List.of(PipelineAttribute.builder().key("env").value("beta4").build()));
    iteration.setAttributes(attributes);

    sut.patchPipelineIteration(projectId, 10L, new PipelineIterationPatchRQ());

    assertSame(attributes, iteration.getAttributes());
    assertNull(iteration.getFinishedAt());
  }

  // ------------------------------------------------------------------ reads

  @Test
  @SuppressWarnings("unchecked")
  void getPipelineIterations_ShouldPageThroughTheAssemblerAndCapTheLimit() {
    var iteration = new PipelineIteration();
    iteration.setId(1L);
    var summary = PipelineIterationSummaryRS.builder().id(1L).build();
    when(pipelineRepository.findById(pipeline.getId())).thenReturn(Optional.of(pipeline));
    when(pipelineIterationRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(iteration)));
    when(viewAssembler.summaries(List.of(iteration))).thenReturn(List.of(summary));

    Page<PipelineIterationSummaryRS> page = sut.getPipelineIterations(projectId, pipeline.getId(),
        PipelineIterationFilter.none(), new OffsetRequest(0, 1000, org.springframework.data.domain.Sort.unsorted()));

    assertEquals(List.of(summary), page.getContent());
    var pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(pipelineIterationRepository).findAll(any(Specification.class), pageable.capture());
    assertEquals(PipelinePaging.MAX_LIMIT, pageable.getValue().getPageSize());
  }

  @Test
  void getPipelineIterations_ForPipelineOfAnotherProject_ShouldThrowNotFound() {
    when(pipelineRepository.findById(pipeline.getId())).thenReturn(Optional.of(pipeline));

    assertThrows(ReportPortalException.class, () -> sut.getPipelineIterations(999L, pipeline.getId(),
        PipelineIterationFilter.none(), new OffsetRequest(0, 10, org.springframework.data.domain.Sort.unsorted())));
  }

  @Test
  void getPipelineIteration_ShouldReturnTheAssembledDetailAndHideOtherProjects() {
    persistedIteration(10L);

    assertSame(renderedDetail, sut.getPipelineIteration(projectId, 10L));
    assertThrows(ReportPortalException.class, () -> sut.getPipelineIteration(999L, 10L));
  }

  @Test
  void compare_ShouldCompareCandidateAgainstBaselineOfTheSamePipeline() {
    var candidate = persistedIteration(10L);
    var baseline = persistedIteration(9L);
    var baselineDetail = PipelineIterationDetailRS.builder().id(9L).build();
    var candidateDetail = PipelineIterationDetailRS.builder().id(10L).build();
    when(viewAssembler.detail(baseline)).thenReturn(baselineDetail);
    when(viewAssembler.detail(candidate)).thenReturn(candidateDetail);
    var compared = PipelineCompareRS.builder().build();
    when(comparator.compare(baselineDetail, candidateDetail)).thenReturn(compared);

    assertSame(compared, sut.comparePipelineIterations(projectId, 10L, 9L));
  }

  @Test
  void compare_OfIterationsFromDifferentPipelines_ShouldThrowBadRequest() {
    persistedIteration(10L);
    var other = new Pipeline();
    other.setId(77L);
    other.setProject(project);
    var baseline = new PipelineIteration();
    baseline.setId(9L);
    baseline.setPipeline(other);
    when(pipelineIterationRepository.findById(9L)).thenReturn(Optional.of(baseline));

    assertThrows(ReportPortalException.class, () -> sut.comparePipelineIterations(projectId, 10L, 9L));
  }
}
